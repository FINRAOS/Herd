/*
* Copyright 2015 herd contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.finra.herd.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.MultiObjectDeleteException.DeleteError;
import com.amazonaws.services.s3.model.MultipartUpload;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.SSEAlgorithm;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectMetadataProvider;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferProgress;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import org.finra.herd.core.HerdDateUtils;
import org.finra.herd.dao.RetryPolicyFactory;
import org.finra.herd.dao.S3Dao;
import org.finra.herd.dao.S3Operations;
import org.finra.herd.dao.helper.AwsHelper;
import org.finra.herd.dao.helper.JavaPropertiesHelper;
import org.finra.herd.model.ObjectNotFoundException;
import org.finra.herd.model.api.xml.AwsCredential;
import org.finra.herd.model.dto.HerdAWSCredentialsProvider;
import org.finra.herd.model.dto.S3FileCopyRequestParamsDto;
import org.finra.herd.model.dto.S3FileTransferRequestParamsDto;
import org.finra.herd.model.dto.S3FileTransferResultsDto;

/**
 * The S3 DAO implementation.
 */
// TODO: Refactor S3 Dao implementation and remove the PMD suppress warning statement below.
@SuppressWarnings("PMD.TooManyMethods")
@Repository
public class S3DaoImpl implements S3Dao
{
    private static final long DEFAULT_SLEEP_INTERVAL_MILLIS = 100;

    private static final Logger LOGGER = LoggerFactory.getLogger(S3DaoImpl.class);

    private static final int MAX_KEYS_PER_DELETE_REQUEST = 1000;

    @Autowired
    private AwsHelper awsHelper;

    @Autowired
    private JavaPropertiesHelper javaPropertiesHelper;

    @Autowired
    private RetryPolicyFactory retryPolicyFactory;

    @Autowired
    private S3Operations s3Operations;

    private long sleepIntervalsMillis = DEFAULT_SLEEP_INTERVAL_MILLIS;

    @Override
    public int abortMultipartUploads(S3FileTransferRequestParamsDto params, Date thresholdDate)
    {
        // Create an Amazon S3 client.
        AmazonS3Client s3Client = getAmazonS3(params);
        int abortedMultipartUploadsCount = 0;

        try
        {
            // List upload markers. Null implies initial list request.
            String uploadIdMarker = null;
            String keyMarker = null;

            boolean truncated;
            do
            {
                // Create the list multipart request, optionally using the last markers.
                ListMultipartUploadsRequest request = new ListMultipartUploadsRequest(params.getS3BucketName());
                request.setUploadIdMarker(uploadIdMarker);
                request.setKeyMarker(keyMarker);

                // Request the multipart upload listing.
                MultipartUploadListing uploadListing = s3Operations.listMultipartUploads(TransferManager.appendSingleObjectUserAgent(request), s3Client);

                for (MultipartUpload upload : uploadListing.getMultipartUploads())
                {
                    if (upload.getInitiated().compareTo(thresholdDate) < 0)
                    {
                        // Abort the upload.
                        s3Operations.abortMultipartUpload(TransferManager
                            .appendSingleObjectUserAgent(new AbortMultipartUploadRequest(params.getS3BucketName(), upload.getKey(), upload.getUploadId())),
                            s3Client);

                        // Log the information about the aborted multipart upload.
                        LOGGER.info("Aborted S3 multipart upload. s3Key=\"{}\" s3BucketName=\"{}\" s3MultipartUploadInitiatedDate=\"{}\"", upload.getKey(),
                            params.getS3BucketName(), upload.getInitiated());

                        // Increment the counter.
                        abortedMultipartUploadsCount++;
                    }
                }

                // Determine whether there are more uploads to list.
                truncated = uploadListing.isTruncated();
                if (truncated)
                {
                    // Record the list markers.
                    uploadIdMarker = uploadListing.getNextUploadIdMarker();
                    keyMarker = uploadListing.getNextKeyMarker();
                }
            }
            while (truncated);
        }
        finally
        {
            // Shutdown the Amazon S3 client instance to release resources.
            s3Client.shutdown();
        }

        return abortedMultipartUploadsCount;
    }

    @Override
    public S3FileTransferResultsDto copyFile(final S3FileCopyRequestParamsDto params) throws InterruptedException
    {
        LOGGER
            .info("Copying S3 object... sourceS3Key=\"{}\" sourceS3BucketName=\"{}\" targetS3Key=\"{}\" targetS3BucketName=\"{}\"", params.getSourceObjectKey(),
                params.getSourceBucketName(), params.getTargetObjectKey(), params.getTargetBucketName());

        // Perform the copy.
        S3FileTransferResultsDto results = performTransfer(params, new Transferer()
        {
            @Override
            public Transfer performTransfer(TransferManager transferManager)
            {
                // Create a copy request.
                CopyObjectRequest copyObjectRequest =
                    new CopyObjectRequest(params.getSourceBucketName(), params.getSourceObjectKey(), params.getTargetBucketName(), params.getTargetObjectKey());

                // If KMS Key ID is specified, set the AWS Key Management System parameters to be used to encrypt the object.
                if (StringUtils.isNotBlank(params.getKmsKeyId()))
                {
                    copyObjectRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(params.getKmsKeyId()));
                }
                // Otherwise, specify the server-side encryption algorithm for encrypting the object using AWS-managed keys.
                else
                {
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
                    copyObjectRequest.setNewObjectMetadata(metadata);
                }

                return s3Operations.copyFile(copyObjectRequest, transferManager);
            }
        });

        LOGGER.info("Copied S3 object. sourceS3Key=\"{}\" sourceS3BucketName=\"{}\" targetS3Key=\"{}\" targetS3BucketName=\"{}\" " +
            "totalBytesTransferred={} transferDuration=\"{}\"", params.getSourceObjectKey(), params.getSourceBucketName(), params.getTargetObjectKey(),
            params.getTargetBucketName(), results.getTotalBytesTransferred(), HerdDateUtils.formatDuration(results.getDurationMillis()));

        logOverallTransferRate(results);

        return results;
    }

    @Override
    public void createDirectory(final S3FileTransferRequestParamsDto params)
    {
        // Create metadata for the directory marker and set content-length to 0 bytes.
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        prepareMetadata(params, metadata);

        // Create empty content.
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

        // Create a PutObjectRequest passing the folder name suffixed by '/'.
        String directoryName = StringUtils.appendIfMissing(params.getS3KeyPrefix(), "/");
        PutObjectRequest putObjectRequest = new PutObjectRequest(params.getS3BucketName(), directoryName, emptyContent, metadata);
        // KMS key ID is being set through prepareMetadata()

        AmazonS3Client s3Client = getAmazonS3(params);

        try
        {
            s3Operations.putObject(putObjectRequest, s3Client);
        }
        catch (AmazonServiceException e)
        {
            throw new IllegalStateException(String
                .format("Failed to create 0 byte S3 object with \"%s\" key in bucket \"%s\". Reason: %s", directoryName, params.getS3BucketName(),
                    e.getMessage()), e);
        }
        finally
        {
            // Shutdown the AmazonS3Client instance to release resources.
            s3Client.shutdown();
        }
    }

    @Override
    public void deleteDirectory(final S3FileTransferRequestParamsDto params)
    {
        LOGGER.info("Deleting keys/key versions from S3... s3KeyPrefix=\"{}\" s3BucketName=\"{}\"", params.getS3KeyPrefix(), params.getS3BucketName());

        Assert.isTrue(!isRootKeyPrefix(params.getS3KeyPrefix()), "Deleting from root directory is not allowed.");

        try
        {
            // List S3 versions.
            List<DeleteObjectsRequest.KeyVersion> keyVersions = listVersions(params);
            LOGGER.info("Found keys/key versions in S3 for deletion. s3KeyCount={} s3KeyPrefix=\"{}\" s3BucketName=\"{}\"", keyVersions.size(),
                params.getS3KeyPrefix(), params.getS3BucketName());

            // In order to avoid a MalformedXML AWS exception, we send delete request only when we have any key versions to delete.
            if (!keyVersions.isEmpty())
            {
                // Create an S3 client.
                AmazonS3Client s3Client = getAmazonS3(params);

                try
                {
                    // Delete the key versions.
                    deleteKeyVersions(s3Client, params.getS3BucketName(), keyVersions);
                }
                finally
                {
                    s3Client.shutdown();
                }
            }
        }
        catch (AmazonClientException e)
        {
            throw new IllegalStateException(String
                .format("Failed to delete keys/key versions with prefix \"%s\" from bucket \"%s\". Reason: %s", params.getS3KeyPrefix(),
                    params.getS3BucketName(), e.getMessage()), e);
        }
    }

    @Override
    public void deleteFileList(final S3FileTransferRequestParamsDto params)
    {
        LOGGER.info("Deleting a list of objects from S3... s3BucketName=\"{}\" s3KeyCount={}", params.getS3BucketName(), params.getFiles().size());

        try
        {
            // In order to avoid a MalformedXML AWS exception, we send delete request only when we have any keys to delete.
            if (!params.getFiles().isEmpty())
            {
                // Create an S3 client.
                AmazonS3Client s3Client = getAmazonS3(params);

                try
                {
                    // Build a list of keys to be deleted.
                    List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
                    for (File file : params.getFiles())
                    {
                        keys.add(new DeleteObjectsRequest.KeyVersion(file.getPath().replaceAll("\\\\", "/")));
                    }

                    // Delete the keys.
                    deleteKeyVersions(s3Client, params.getS3BucketName(), keys);
                }
                finally
                {
                    s3Client.shutdown();
                }
            }
        }
        catch (Exception e)
        {
            throw new IllegalStateException(
                String.format("Failed to delete a list of keys from bucket \"%s\". Reason: %s", params.getS3BucketName(), e.getMessage()), e);
        }
    }

    @Override
    public S3FileTransferResultsDto downloadDirectory(final S3FileTransferRequestParamsDto params) throws InterruptedException
    {
        LOGGER.info("Downloading S3 directory to the local system... s3KeyPrefix=\"{}\" s3BucketName=\"{}\" localDirectory=\"{}\"", params.getS3KeyPrefix(),
            params.getS3BucketName(), params.getLocalPath());

        // Note that the directory download always recursively copies sub-directories.
        // To not recurse, we would have to list the files on S3 (AmazonS3Client.html#listObjects) and manually copy them one at a time.

        // Perform the transfer.
        S3FileTransferResultsDto results = performTransfer(params, new Transferer()
        {
            @Override
            public Transfer performTransfer(TransferManager transferManager)
            {
                return s3Operations.downloadDirectory(params.getS3BucketName(), params.getS3KeyPrefix(), new File(params.getLocalPath()), transferManager);
            }
        });

        LOGGER.info("Downloaded S3 directory to the local system. " +
            "s3KeyPrefix=\"{}\" s3BucketName=\"{}\" localDirectory=\"{}\" s3KeyCount={} totalBytesTransferred={} transferDuration=\"{}\"",
            params.getS3KeyPrefix(), params.getS3BucketName(), params.getLocalPath(), results.getTotalFilesTransferred(), results.getTotalBytesTransferred(),
            HerdDateUtils.formatDuration(results.getDurationMillis()));

        logOverallTransferRate(results);

        return results;
    }

    @Override
    public S3FileTransferResultsDto downloadFile(final S3FileTransferRequestParamsDto params) throws InterruptedException
    {
        LOGGER.info("Downloading S3 file... s3Key=\"{}\" s3BucketName=\"{}\" localPath=\"{}\"", params.getS3KeyPrefix(), params.getS3BucketName(),
            params.getLocalPath());

        // Perform the transfer.
        S3FileTransferResultsDto results = performTransfer(params, new Transferer()
        {
            @Override
            public Transfer performTransfer(TransferManager transferManager)
            {
                return s3Operations.download(params.getS3BucketName(), params.getS3KeyPrefix(), new File(params.getLocalPath()), transferManager);
            }
        });

        LOGGER
            .info("Downloaded S3 file to the local system. s3Key=\"{}\" s3BucketName=\"{}\" localPath=\"{}\" totalBytesTransferred={} transferDuration=\"{}\"",
                params.getS3KeyPrefix(), params.getS3BucketName(), params.getLocalPath(), results.getTotalBytesTransferred(),
                HerdDateUtils.formatDuration(results.getDurationMillis()));

        logOverallTransferRate(results);

        return results;
    }

    @Override
    public String generateGetObjectPresignedUrl(String bucketName, String key, Date expiration, S3FileTransferRequestParamsDto s3FileTransferRequestParamsDto)
    {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key, HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expiration);
        AmazonS3Client s3 = getAmazonS3(s3FileTransferRequestParamsDto);
        try
        {
            return s3Operations.generatePresignedUrl(generatePresignedUrlRequest, s3).toString();
        }
        finally
        {
            s3.shutdown();
        }
    }

    @Override
    public ObjectMetadata getObjectMetadata(final S3FileTransferRequestParamsDto params)
    {
        AmazonS3Client s3Client = getAmazonS3(params);

        try
        {
            return s3Operations.getObjectMetadata(params.getS3BucketName(), params.getS3KeyPrefix(), s3Client);
        }
        catch (AmazonServiceException e)
        {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND)
            {
                return null;
            }

            throw new IllegalStateException(String
                .format("Failed to get S3 metadata for object key \"%s\" from bucket \"%s\". Reason: %s", params.getS3KeyPrefix(), params.getS3BucketName(),
                    e.getMessage()), e);
        }
        finally
        {
            // Shutdown the AmazonS3Client instance to release resources.
            s3Client.shutdown();
        }
    }

    @Override
    public Properties getProperties(String bucketName, String key, S3FileTransferRequestParamsDto s3FileTransferRequestParamsDto)
    {
        AmazonS3Client s3Client = getAmazonS3(s3FileTransferRequestParamsDto);

        try
        {
            S3Object s3Object = getS3Object(s3Client, bucketName, key, true);
            return javaPropertiesHelper.getProperties(s3Object.getObjectContent());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The properties file in S3 bucket '" + bucketName + "' and key '" + key + "' is invalid.", e);
        }
        finally
        {
            s3Client.shutdown();
        }
    }

    @Override
    public List<S3ObjectSummary> listDirectory(final S3FileTransferRequestParamsDto params)
    {
        // By default, we do not ignore 0 byte objects that represent S3 directories.
        return listDirectory(params, false);
    }

    @Override
    public List<S3ObjectSummary> listDirectory(final S3FileTransferRequestParamsDto params, boolean ignoreZeroByteDirectoryMarkers)
    {
        Assert.isTrue(!isRootKeyPrefix(params.getS3KeyPrefix()), "Listing of S3 objects from root directory is not allowed.");

        AmazonS3Client s3Client = getAmazonS3(params);
        List<S3ObjectSummary> s3ObjectSummaries = new ArrayList<>();

        try
        {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(params.getS3BucketName()).withPrefix(params.getS3KeyPrefix());
            ObjectListing objectListing;

            do
            {
                objectListing = s3Operations.listObjects(listObjectsRequest, s3Client);

                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                {
                    // Ignore 0 byte objects that represent S3 directories.
                    if (!(ignoreZeroByteDirectoryMarkers && objectSummary.getKey().endsWith("/") && objectSummary.getSize() == 0L))
                    {
                        s3ObjectSummaries.add(objectSummary);
                    }
                }

                listObjectsRequest.setMarker(objectListing.getNextMarker());
            }
            while (objectListing.isTruncated());
        }
        catch (AmazonS3Exception amazonS3Exception)
        {
            if (S3Operations.ERROR_CODE_NO_SUCH_BUCKET.equals(amazonS3Exception.getErrorCode()))
            {
                throw new IllegalArgumentException("The specified bucket '" + params.getS3BucketName() + "' does not exist.", amazonS3Exception);
            }
            throw new IllegalStateException("Error accessing S3", amazonS3Exception);
        }
        catch (AmazonClientException e)
        {
            throw new IllegalStateException(String
                .format("Failed to list keys with prefix \"%s\" from bucket \"%s\". Reason: %s", params.getS3KeyPrefix(), params.getS3BucketName(),
                    e.getMessage()), e);
        }
        finally
        {
            // Shutdown the AmazonS3Client instance to release resources.
            s3Client.shutdown();
        }

        return s3ObjectSummaries;
    }

    @Override
    public List<DeleteObjectsRequest.KeyVersion> listVersions(final S3FileTransferRequestParamsDto params)
    {
        Assert.isTrue(!isRootKeyPrefix(params.getS3KeyPrefix()), "Listing of S3 key versions from root directory is not allowed.");

        AmazonS3Client s3Client = getAmazonS3(params);
        List<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();

        try
        {
            ListVersionsRequest listVersionsRequest = new ListVersionsRequest().withBucketName(params.getS3BucketName()).withPrefix(params.getS3KeyPrefix());
            VersionListing versionListing;

            do
            {
                versionListing = s3Operations.listVersions(listVersionsRequest, s3Client);

                for (S3VersionSummary versionSummary : versionListing.getVersionSummaries())
                {
                    keyVersions.add(new DeleteObjectsRequest.KeyVersion(versionSummary.getKey(), versionSummary.getVersionId()));
                }

                listVersionsRequest.setKeyMarker(versionListing.getNextKeyMarker());
                listVersionsRequest.setVersionIdMarker(versionListing.getNextVersionIdMarker());
            }
            while (versionListing.isTruncated());
        }
        catch (AmazonS3Exception amazonS3Exception)
        {
            if (S3Operations.ERROR_CODE_NO_SUCH_BUCKET.equals(amazonS3Exception.getErrorCode()))
            {
                throw new IllegalArgumentException("The specified bucket '" + params.getS3BucketName() + "' does not exist.", amazonS3Exception);
            }
            throw new IllegalStateException("Error accessing S3", amazonS3Exception);
        }
        catch (AmazonClientException e)
        {
            throw new IllegalStateException(String
                .format("Failed to list keys/key versions with prefix \"%s\" from bucket \"%s\". Reason: %s", params.getS3KeyPrefix(), params.getS3BucketName(),
                    e.getMessage()), e);
        }
        finally
        {
            // Shutdown the AmazonS3Client instance to release resources.
            s3Client.shutdown();
        }

        return keyVersions;
    }

    @Override
    public void restoreObjects(final S3FileTransferRequestParamsDto params, int expirationInDays)
    {
        LOGGER.info("Restoring a list of objects in S3... s3KeyPrefix=\"{}\" s3BucketName=\"{}\" s3KeyCount={}", params.getS3KeyPrefix(),
            params.getS3BucketName(), params.getFiles().size());

        if (!CollectionUtils.isEmpty(params.getFiles()))
        {
            // Initialize a key value pair for the error message in the catch block.
            String key = params.getFiles().get(0).getPath().replaceAll("\\\\", "/");

            try
            {
                // Create an S3 client.
                AmazonS3Client s3Client = getAmazonS3(params);

                // Create a restore object request.
                RestoreObjectRequest requestRestore = new RestoreObjectRequest(params.getS3BucketName(), null, expirationInDays);

                try
                {
                    for (File file : params.getFiles())
                    {
                        key = file.getPath().replaceAll("\\\\", "/");
                        ObjectMetadata objectMetadata = s3Operations.getObjectMetadata(params.getS3BucketName(), key, s3Client);

                        // Request a restore for objects that are not already being restored.
                        if (BooleanUtils.isNotTrue(objectMetadata.getOngoingRestore()))
                        {
                            requestRestore.setKey(key);
                            s3Operations.restoreObject(requestRestore, s3Client);
                        }
                    }
                }
                finally
                {
                    s3Client.shutdown();
                }
            }
            catch (Exception e)
            {
                throw new IllegalStateException(String
                    .format("Failed to initiate a restore request for \"%s\" key in \"%s\" bucket. Reason: %s", key, params.getS3BucketName(), e.getMessage()),
                    e);
            }
        }
    }

    @Override
    public boolean s3FileExists(S3FileTransferRequestParamsDto params) throws RuntimeException
    {
        AmazonS3Client s3Client = getAmazonS3(params);

        try
        {
            S3Object s3Object = getS3Object(s3Client, params.getS3BucketName(), params.getS3KeyPrefix(), false);
            return (s3Object != null);
        }
        finally
        {
            s3Client.shutdown();
        }
    }

    @Override
    public void tagObjects(final S3FileTransferRequestParamsDto s3FileTransferRequestParamsDto, final S3FileTransferRequestParamsDto s3ObjectTaggerParamsDto,
        final Tag tag)
    {
        LOGGER.info("Tagging objects in S3... s3BucketName=\"{}\" s3KeyCount={} s3ObjectTagKey=\"{}\" s3ObjectTagValue=\"{}\"",
            s3FileTransferRequestParamsDto.getS3BucketName(), s3FileTransferRequestParamsDto.getFiles().size(), tag.getKey(), tag.getValue());

        if (!CollectionUtils.isEmpty(s3FileTransferRequestParamsDto.getFiles()))
        {
            // Initialize a key value pair for the error message in the catch block.
            String s3Key = s3FileTransferRequestParamsDto.getFiles().get(0).getPath().replaceAll("\\\\", "/");

            // Amazon S3 client to access S3 objects.
            AmazonS3Client s3Client = null;

            // Amazon S3 client for S3 object tagging.
            AmazonS3Client s3ObjectTaggerClient = null;

            try
            {
                // Create an S3 client to access S3 objects.
                s3Client = getAmazonS3(s3FileTransferRequestParamsDto);

                // Create an S3 client for S3 object tagging.
                s3ObjectTaggerClient = getAmazonS3(s3ObjectTaggerParamsDto);

                // Create a get object tagging request.
                GetObjectTaggingRequest getObjectTaggingRequest = new GetObjectTaggingRequest(s3FileTransferRequestParamsDto.getS3BucketName(), null);

                // Create a restore object request.
                SetObjectTaggingRequest setObjectTaggingRequest = new SetObjectTaggingRequest(s3FileTransferRequestParamsDto.getS3BucketName(), null, null);

                for (File file : s3FileTransferRequestParamsDto.getFiles())
                {
                    // Prepare an S3 key.
                    s3Key = file.getPath().replaceAll("\\\\", "/");

                    // Retrieve the current tagging information for the S3 key.
                    getObjectTaggingRequest.setKey(s3Key);
                    GetObjectTaggingResult getObjectTaggingResult = s3Operations.getObjectTagging(getObjectTaggingRequest, s3Client);

                    // Update the list of tags to include the specified S3 object tag.
                    List<Tag> updatedTags = new ArrayList<>();
                    updatedTags.add(tag);
                    if (CollectionUtils.isNotEmpty(getObjectTaggingResult.getTagSet()))
                    {
                        for (Tag currentTag : getObjectTaggingResult.getTagSet())
                        {
                            if (!StringUtils.equals(tag.getKey(), currentTag.getKey()))
                            {
                                updatedTags.add(currentTag);
                            }
                        }
                    }

                    // Update the tagging information.
                    setObjectTaggingRequest.setKey(s3Key);
                    setObjectTaggingRequest.setTagging(new ObjectTagging(updatedTags));
                    s3Operations.setObjectTagging(setObjectTaggingRequest, s3ObjectTaggerClient);
                }
            }
            catch (Exception e)
            {
                throw new IllegalStateException(String
                    .format("Failed to tag S3 object with \"%s\" key in \"%s\" bucket. Reason: %s", s3Key, s3FileTransferRequestParamsDto.getS3BucketName(),
                        e.getMessage()), e);
            }
            finally
            {
                if (s3Client != null)
                {
                    s3Client.shutdown();
                }

                if (s3ObjectTaggerClient != null)
                {
                    s3ObjectTaggerClient.shutdown();
                }
            }
        }
    }

    @Override
    public S3FileTransferResultsDto uploadDirectory(final S3FileTransferRequestParamsDto params) throws InterruptedException
    {
        LOGGER.info("Uploading local directory to S3... localDirectory=\"{}\" s3KeyPrefix=\"{}\" s3BucketName=\"{}\"", params.getLocalPath(),
            params.getS3KeyPrefix(), params.getS3BucketName());

        // Perform the transfer.
        S3FileTransferResultsDto results = performTransfer(params, new Transferer()
        {
            @Override
            public Transfer performTransfer(TransferManager transferManager)
            {
                return s3Operations.uploadDirectory(params.getS3BucketName(), params.getS3KeyPrefix(), new File(params.getLocalPath()), params.isRecursive(),
                    new ObjectMetadataProvider()
                    {
                        @Override
                        public void provideObjectMetadata(File file, ObjectMetadata metadata)
                        {
                            prepareMetadata(params, metadata);
                        }
                    }, transferManager);
            }
        });

        LOGGER.info("Uploaded local directory to S3. " +
            "localDirectory=\"{}\" s3KeyPrefix=\"{}\" s3BucketName=\"{}\" s3KeyCount={} totalBytesTransferred={} transferDuration=\"{}\"",
            params.getLocalPath(), params.getS3KeyPrefix(), params.getS3BucketName(), results.getTotalFilesTransferred(), results.getTotalBytesTransferred(),
            HerdDateUtils.formatDuration(results.getDurationMillis()));

        logOverallTransferRate(results);

        return results;
    }

    @Override
    public S3FileTransferResultsDto uploadFile(final S3FileTransferRequestParamsDto params) throws InterruptedException
    {
        LOGGER.info("Uploading local file to S3... localPath=\"{}\" s3Key=\"{}\" s3BucketName=\"{}\"", params.getLocalPath(), params.getS3KeyPrefix(),
            params.getS3BucketName());

        // Perform the transfer.
        S3FileTransferResultsDto results = performTransfer(params, new Transferer()
        {
            @Override
            public Transfer performTransfer(TransferManager transferManager)
            {
                // Get a handle to the local file.
                File localFile = new File(params.getLocalPath());

                // Create and prepare the metadata.
                ObjectMetadata metadata = new ObjectMetadata();
                prepareMetadata(params, metadata);

                // Create a put request and a transfer manager with the parameters and the metadata.
                PutObjectRequest putObjectRequest = new PutObjectRequest(params.getS3BucketName(), params.getS3KeyPrefix(), localFile);
                putObjectRequest.setMetadata(metadata);

                return s3Operations.upload(putObjectRequest, transferManager);
            }
        });

        LOGGER.info("Uploaded local file to the S3. localPath=\"{}\" s3Key=\"{}\" s3BucketName=\"{}\" totalBytesTransferred={} transferDuration=\"{}\"",
            params.getLocalPath(), params.getS3KeyPrefix(), params.getS3BucketName(), results.getTotalBytesTransferred(),
            HerdDateUtils.formatDuration(results.getDurationMillis()));

        logOverallTransferRate(results);

        return results;
    }

    @Override
    public S3FileTransferResultsDto uploadFileList(final S3FileTransferRequestParamsDto params) throws InterruptedException
    {
        LOGGER.info("Uploading a list of files from the local directory to S3... localDirectory=\"{}\" s3KeyPrefix=\"{}\" s3BucketName=\"{}\" s3KeyCount={}",
            params.getLocalPath(), params.getS3KeyPrefix(), params.getS3BucketName(), params.getFiles().size());

        if (LOGGER.isInfoEnabled())
        {
            for (File file : params.getFiles())
            {
                LOGGER.info("s3Key=\"{}\"", file.getPath());
            }
        }

        // Perform the transfer.
        S3FileTransferResultsDto results = performTransfer(params, new Transferer()
        {
            @Override
            public Transfer performTransfer(TransferManager transferManager)
            {
                return s3Operations.uploadFileList(params.getS3BucketName(), params.getS3KeyPrefix(), new File(params.getLocalPath()), params.getFiles(),
                    new ObjectMetadataProvider()
                    {
                        @Override
                        public void provideObjectMetadata(File file, ObjectMetadata metadata)
                        {
                            prepareMetadata(params, metadata);
                        }
                    }, transferManager);
            }
        });

        LOGGER.info("Uploaded list of files from the local directory to S3. " +
            "localDirectory=\"{}\" s3KeyPrefix=\"{}\" s3BucketName=\"{}\" s3KeyCount={} totalBytesTransferred={} transferDuration=\"{}\"",
            params.getLocalPath(), params.getS3KeyPrefix(), params.getS3BucketName(), results.getTotalFilesTransferred(), results.getTotalBytesTransferred(),
            HerdDateUtils.formatDuration(results.getDurationMillis()));

        logOverallTransferRate(results);

        return results;
    }

    @Override
    public void validateGlacierS3FilesRestored(S3FileTransferRequestParamsDto params) throws RuntimeException
    {
        LOGGER.info("Checking for already restored Glacier storage class objects... s3KeyPrefix=\"{}\" s3BucketName=\"{}\" s3KeyCount={}",
            params.getS3KeyPrefix(), params.getS3BucketName(), params.getFiles().size());

        if (!CollectionUtils.isEmpty(params.getFiles()))
        {
            // Initialize a key value pair for the error message in the catch block.
            String key = params.getFiles().get(0).getPath().replaceAll("\\\\", "/");

            try
            {
                // Create an S3 client.
                AmazonS3Client s3Client = getAmazonS3(params);

                try
                {
                    for (File file : params.getFiles())
                    {
                        key = file.getPath().replaceAll("\\\\", "/");
                        ObjectMetadata objectMetadata = s3Operations.getObjectMetadata(params.getS3BucketName(), key, s3Client);

                        // Fail if a not already restored object is detected.
                        if (BooleanUtils.isNotFalse(objectMetadata.getOngoingRestore()))
                        {
                            throw new IllegalArgumentException(String
                                .format("Archived Glacier S3 file \"%s\" is not restored. StorageClass {%s}, OngoingRestore flag {%s}, S3 bucket name {%s}",
                                    key, objectMetadata.getStorageClass(), objectMetadata.getOngoingRestore(), params.getS3BucketName()));
                        }
                    }
                }
                finally
                {
                    s3Client.shutdown();
                }
            }
            catch (AmazonServiceException e)
            {
                throw new IllegalStateException(
                    String.format("Fail to check restore status for \"%s\" key in \"%s\" bucket. Reason: %s", key, params.getS3BucketName(), e.getMessage()),
                    e);
            }
        }
    }

    @Override
    public void validateS3File(S3FileTransferRequestParamsDto params, Long fileSizeInBytes) throws RuntimeException
    {
        ObjectMetadata objectMetadata = getObjectMetadata(params);

        if (objectMetadata == null)
        {
            throw new ObjectNotFoundException(String.format("File not found at s3://%s/%s location.", params.getS3BucketName(), params.getS3KeyPrefix()));
        }

        Assert.isTrue(fileSizeInBytes == null || Objects.equals(fileSizeInBytes, objectMetadata.getContentLength()), String
            .format("Specified file size (%d bytes) does not match to the actual file size (%d bytes) reported by S3 for s3://%s/%s file.", fileSizeInBytes,
                objectMetadata.getContentLength(), params.getS3BucketName(), params.getS3KeyPrefix()));
    }

    /**
     * Returns true is S3 key prefix is a root.
     *
     * @param s3KeyPrefix the S3 key prefix to be validated
     *
     * @return true if S3 key prefix is a root; false otherwise
     */
    protected boolean isRootKeyPrefix(String s3KeyPrefix)
    {
        return StringUtils.isBlank(s3KeyPrefix) || s3KeyPrefix.equals("/");
    }

    /**
     * Deletes a list of keys/key versions from the specified S3 bucket.
     *
     * @param s3Client the S3 client
     * @param s3BucketName the S3 bucket name
     * @param keyVersions the list of S3 keys/key versions
     */
    private void deleteKeyVersions(AmazonS3Client s3Client, String s3BucketName, List<DeleteObjectsRequest.KeyVersion> keyVersions)
    {
        // Create a request to delete multiple objects in the specified bucket.
        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(s3BucketName);

        // The Multi-Object Delete request can contain a list of up to 1000 keys.
        for (int i = 0; i < keyVersions.size() / MAX_KEYS_PER_DELETE_REQUEST + 1; i++)
        {
            List<DeleteObjectsRequest.KeyVersion> keysSubList =
                keyVersions.subList(i * MAX_KEYS_PER_DELETE_REQUEST, Math.min(keyVersions.size(), (i + 1) * MAX_KEYS_PER_DELETE_REQUEST));
            multiObjectDeleteRequest.setKeys(keysSubList);
            try
            {
                s3Operations.deleteObjects(multiObjectDeleteRequest, s3Client);
            }
            catch (MultiObjectDeleteException multiObjectDeleteException)
            {
                logMultiObjectDeleteException(multiObjectDeleteException);
                throw multiObjectDeleteException;
            }

            LOGGER.info("Successfully requested the deletion of the listed below keys/key versions from the S3 bucket. s3KeyCount={} s3BucketName=\"{}\"",
                keysSubList.size(), s3BucketName);

            for (DeleteObjectsRequest.KeyVersion keyVersion : keysSubList)
            {
                LOGGER.info("s3Key=\"{}\" s3VersionId=\"{}\"", keyVersion.getKey(), keyVersion.getVersion());
            }
        }
    }

    /**
     * <p> Gets the {@link AWSCredentialsProvider} based on the credentials in the given parameters. </p> <p> Returns {@link DefaultAWSCredentialsProviderChain}
     * if either access or secret key is {@code null}. Otherwise returns a {@link StaticCredentialsProvider} with the credentials. </p>
     *
     * @param params - Access parameters
     *
     * @return AWS credentials provider implementation
     */
    private AWSCredentialsProvider getAWSCredentialsProvider(S3FileTransferRequestParamsDto params)
    {
        List<AWSCredentialsProvider> providers = new ArrayList<>();
        String accessKey = params.getAwsAccessKeyId();
        String secretKey = params.getAwsSecretKey();
        if (accessKey != null && secretKey != null)
        {
            providers.add(new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
        }
        for (HerdAWSCredentialsProvider herdAWSCredentialsProvider : params.getAdditionalAwsCredentialsProviders())
        {
            providers.add(new HerdAwsCredentialsProviderWrapper(herdAWSCredentialsProvider));
        }
        providers.add(new DefaultAWSCredentialsProviderChain());
        return new AWSCredentialsProviderChain(providers.toArray(new AWSCredentialsProvider[providers.size()]));
    }

    /**
     * Gets a new S3 client based on the specified parameters. The HTTP proxy information will be added if the host and port are specified in the parameters.
     *
     * @param params the parameters.
     *
     * @return the Amazon S3 client.
     */
    private AmazonS3Client getAmazonS3(S3FileTransferRequestParamsDto params)
    {
        AmazonS3Client amazonS3Client;

        ClientConfiguration clientConfiguration = new ClientConfiguration().withRetryPolicy(retryPolicyFactory.getRetryPolicy());

        // Set the proxy configuration, if proxy is specified.
        if (StringUtils.isNotBlank(params.getHttpProxyHost()) && params.getHttpProxyPort() != null)
        {
            clientConfiguration.setProxyHost(params.getHttpProxyHost());
            clientConfiguration.setProxyPort(params.getHttpProxyPort());
        }

        // Sign all S3 API's with V4 signing.
        // AmazonS3Client.upgradeToSigV4 already has some scenarios where it will "upgrade" the signing approach to use V4 if not already present (e.g.
        // GetObjectRequest and KMS PutObjectRequest), but setting it here (especially when KMS is used) will ensure it isn't missed when required (e.g.
        // copying objects between KMS encrypted buckets). Otherwise, AWS will return a bad request error and retry which isn't desirable.
        clientConfiguration.setSignerOverride(SIGNER_OVERRIDE_V4);

        // Set the optional socket timeout, if configured.
        if (params.getSocketTimeout() != null)
        {
            clientConfiguration.setSocketTimeout(params.getSocketTimeout());
        }

        // Create an S3 client using passed in credentials and HTTP proxy information.
        if (StringUtils.isNotBlank(params.getAwsAccessKeyId()) && StringUtils.isNotBlank(params.getAwsSecretKey()) &&
            StringUtils.isNotBlank(params.getSessionToken()))
        {
            // Create an S3 client using basic session credentials.
            amazonS3Client = new AmazonS3Client(new BasicSessionCredentials(params.getAwsAccessKeyId(), params.getAwsSecretKey(), params.getSessionToken()),
                clientConfiguration);
        }
        else
        {
            // Create an S3 client using AWS credentials provider.
            amazonS3Client = new AmazonS3Client(getAWSCredentialsProvider(params), clientConfiguration);
        }

        // Set the optional endpoint, if specified.
        if (StringUtils.isNotBlank(params.getS3Endpoint()))
        {
            LOGGER.info("Configured S3 Endpoint: " + params.getS3Endpoint());
            amazonS3Client.setEndpoint(params.getS3Endpoint());
        }

        // Return the newly created client.
        return amazonS3Client;
    }

    /**
     * Retrieves an S3 object.
     *
     * @param s3Client the S3 client
     * @param bucketName the S3 bucket name
     * @param key the S3 object key
     * @param errorOnNoSuchKey true to throw an error when the object key is not found, otherwise return null
     *
     * @return the S3 object
     * @throws ObjectNotFoundException when specified bucket or key does not exist or access to bucket or key is denied
     */
    private S3Object getS3Object(AmazonS3Client s3Client, String bucketName, String key, boolean errorOnNoSuchKey)
    {
        try
        {
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            return s3Operations.getS3Object(getObjectRequest, s3Client);
        }
        catch (AmazonServiceException amazonServiceException)
        {
            String errorCode = amazonServiceException.getErrorCode();

            if (S3Operations.ERROR_CODE_ACCESS_DENIED.equals(errorCode))
            {
                throw new ObjectNotFoundException("Application does not have access to the specified S3 object at bucket '" + bucketName + "' and key '" +
                    key + "'.", amazonServiceException);
            }
            else if (S3Operations.ERROR_CODE_NO_SUCH_BUCKET.equals(errorCode))
            {
                throw new ObjectNotFoundException("Specified S3 bucket '" + bucketName + "' does not exist.", amazonServiceException);
            }
            else if (S3Operations.ERROR_CODE_NO_SUCH_KEY.equals(errorCode))
            {
                if (errorOnNoSuchKey)
                {
                    throw new ObjectNotFoundException("Specified S3 object key '" + key + "' does not exist.", amazonServiceException);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                throw amazonServiceException;
            }
        }
    }

    /**
     * Gets a transfer manager with the specified parameters including proxy host, proxy port, S3 access key, S3 secret key, and max threads.
     *
     * @param params the parameters.
     *
     * @return a newly created transfer manager.
     */
    private TransferManager getTransferManager(final S3FileTransferRequestParamsDto params)
    {
        // We are returning a new transfer manager each time it is called. Although the Javadocs of TransferManager say to share a single instance
        // if possible, this could potentially be a problem if TransferManager.shutdown(true) is called and underlying resources are not present when needed
        // for subsequent transfers.
        if (params.getMaxThreads() == null)
        {
            // Create a transfer manager that will internally use an appropriate number of threads.
            return new TransferManager(getAmazonS3(params));
        }
        else
        {
            // Create a transfer manager with our own executor configured with the specified total threads.
            LOGGER.info("Creating a transfer manager. fixedThreadPoolSize={}", params.getMaxThreads());
            return new TransferManager(getAmazonS3(params), Executors.newFixedThreadPool(params.getMaxThreads()));
        }
    }

    /**
     * Logs the given MultiObjectDeleteException.
     *
     * @param multiObjectDeleteException The exception to log
     */
    private void logMultiObjectDeleteException(MultiObjectDeleteException multiObjectDeleteException)
    {
        // Create and initialize a string buffer. The initialization is required here in order to avoid an InsufficientStringBufferDeclaration PMD violation.
        StringBuilder builder = new StringBuilder(128);
        builder.append(String.format("Error deleting multiple objects. Below are the list of objects which failed to delete.%n"));
        List<DeleteError> deleteErrors = multiObjectDeleteException.getErrors();
        for (DeleteError deleteError : deleteErrors)
        {
            String key = deleteError.getKey();
            String versionId = deleteError.getVersionId();
            String code = deleteError.getCode();
            String message = deleteError.getMessage();
            builder
                .append(String.format("s3Key=\"%s\" s3VersionId=\"%s\" s3DeleteErrorCode=\"%s\" s3DeleteErrorMessage=\"%s\"%n", key, versionId, code, message));
        }
        LOGGER.error(builder.toString());
    }

    /**
     * Logs overall transfer rate for an S3 file transfer operation.
     *
     * @param s3FileTransferResultsDto the DTO for the S3 file transfer operation results
     */
    private void logOverallTransferRate(S3FileTransferResultsDto s3FileTransferResultsDto)
    {
        if (LOGGER.isInfoEnabled())
        {
            NumberFormat formatter = new DecimalFormat("#0.00");

            LOGGER.info("overallTransferRateKiloBytesPerSecond={} overallTransferRateMegaBitsPerSecond={}", formatter.format(awsHelper
                .getTransferRateInKilobytesPerSecond(s3FileTransferResultsDto.getTotalBytesTransferred(), s3FileTransferResultsDto.getDurationMillis())),
                formatter.format(awsHelper
                    .getTransferRateInMegabitsPerSecond(s3FileTransferResultsDto.getTotalBytesTransferred(), s3FileTransferResultsDto.getDurationMillis())));
        }
    }

    /**
     * Logs transfer progress for an S3 file transfer operation.
     *
     * @param transferProgress the progress of an S3 transfer operation
     */
    private void logTransferProgress(TransferProgress transferProgress)
    {
        // If the total bytes to transfer is set to 0, we do not log the transfer progress.
        if (LOGGER.isInfoEnabled() && transferProgress.getTotalBytesToTransfer() > 0)
        {
            NumberFormat formatter = new DecimalFormat("#0.0");

            LOGGER.info("progressBytesTransferred={} totalBytesToTransfer={} progressPercentTransferred={}", transferProgress.getBytesTransferred(),
                transferProgress.getTotalBytesToTransfer(), formatter.format(transferProgress.getPercentTransferred()));
        }
    }

    /**
     * Performs a file/directory transfer.
     *
     * @param params the parameters.
     * @param transferer a transferer that knows how to perform the transfer.
     *
     * @return the results.
     * @throws InterruptedException if a problem is encountered.
     */
    private S3FileTransferResultsDto performTransfer(final S3FileTransferRequestParamsDto params, Transferer transferer) throws InterruptedException
    {
        // Create a transfer manager.
        TransferManager transferManager = getTransferManager(params);

        try
        {
            // Start a stop watch to keep track of how long the transfer takes.
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // Perform the transfer.
            Transfer transfer = transferer.performTransfer(transferManager);
            TransferProgress transferProgress = transfer.getProgress();

            logTransferProgress(transferProgress);

            long stepCount = 0;

            // Loop until the transfer is complete.
            do
            {
                Thread.sleep(sleepIntervalsMillis);
                stepCount++;

                // Log progress status every 30 seconds and when transfer is complete.
                if (transfer.isDone() || stepCount % 300 == 0)
                {
                    logTransferProgress(transferProgress);
                }
            }
            while (!transfer.isDone());

            // Stop the stop watch and create a results object.
            stopWatch.stop();

            // If the transfer failed, throw the underlying AWS exception if we can determine one. Otherwise, throw our own exception.
            TransferState transferState = transfer.getState();
            if (transferState == TransferState.Failed)
            {
                // The waitForException method should return the underlying AWS exception since the state is "Failed". It should not block since the
                // transfer is already "done" per previous code checking "isDone".
                AmazonClientException amazonClientException = transfer.waitForException();

                // If the returned exception is null, we weren't able to get the underlying AWS exception so just throw our own exception.
                // This is unlikely since the transfer failed, but it's better to handle the possibility just in case.
                if (amazonClientException == null)
                {
                    throw new IllegalStateException("The transfer operation \"" + transfer.getDescription() + "\" failed for an unknown reason.");
                }

                // Throw the Amazon underlying exception.
                throw amazonClientException;
            }
            // Ensure the transfer completed. If not, throw an exception.
            else if (transferState != TransferState.Completed)
            {
                throw new IllegalStateException(
                    "The transfer operation \"" + transfer.getDescription() + "\" did not complete successfully. Current state: \"" + transferState +
                        "\".");
            }

            // TransferProgress.getBytesTransferred() are not populated for S3 Copy objects.
            if (!(transfer instanceof Copy))
            {
                // Sanity check for the number of bytes transferred.
                Assert.isTrue(transferProgress.getBytesTransferred() >= transferProgress.getTotalBytesToTransfer(), String
                    .format("Actual number of bytes transferred is less than expected (actual: %d bytes; expected: %d bytes).",
                        transferProgress.getBytesTransferred(), transferProgress.getTotalBytesToTransfer()));
            }

            // Create the results object and populate it with the standard data.
            S3FileTransferResultsDto results = new S3FileTransferResultsDto();
            results.setDurationMillis(stopWatch.getTime());
            results.setTotalBytesTransferred(transfer.getProgress().getBytesTransferred());
            results.setTotalFilesTransferred(1L);

            if (transfer instanceof MultipleFileUpload)
            {
                // For upload directory, we need to calculate the total number of files transferred differently.
                results.setTotalFilesTransferred((long) ((MultipleFileUpload) transfer).getSubTransfers().size());
            }
            else if (transfer instanceof MultipleFileDownload)
            {
                // For download directory, we need to calculate the total number of files differently.
                results.setTotalFilesTransferred((long) listDirectory(params).size());
            }

            // Return the results.
            return results;
        }
        finally
        {
            // Shutdown the transfer manager to release resources. If this isn't done, the JVM may delay upon exiting.
            transferManager.shutdownNow();
        }
    }

    /**
     * Prepares the object metadata for server side encryption and reduced redundancy storage.
     *
     * @param params the parameters.
     * @param metadata the metadata to prepare.
     */
    private void prepareMetadata(final S3FileTransferRequestParamsDto params, ObjectMetadata metadata)
    {
        // Set the server side encryption
        if (params.getKmsKeyId() != null)
        {
            /*
             * TODO Use proper way to set KMS once AWS provides a way.
             * We are modifying the raw headers directly since TransferManager's uploadFileList operation does not provide a way to set a KMS key ID.
             * This would normally cause some issues when uploading where an MD5 checksum validation exception will be thrown, even though the object is
             * correctly uploaded.
             * To get around this, a system property defined at
             * com.amazonaws.services.s3.internal.SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY must be set.
             */
            metadata.setSSEAlgorithm(SSEAlgorithm.KMS.getAlgorithm());
            metadata.setHeader(Headers.SERVER_SIDE_ENCRYPTION_AWS_KMS_KEYID, params.getKmsKeyId().trim());
        }
        else
        {
            metadata.setSSEAlgorithm(SSEAlgorithm.AES256.getAlgorithm());
        }

        // If specified, set the metadata to use RRS.
        if (Boolean.TRUE.equals(params.isUseRrs()))
        {
            // TODO: For upload File, we can set RRS on the putObjectRequest. For uploadDirectory, this is the only
            // way to do it. However, setHeader() is flagged as For Internal Use Only
            metadata.setHeader(Headers.STORAGE_CLASS, StorageClass.ReducedRedundancy.toString());
        }
    }

    /**
     * An object that can perform a transfer using a transform manager.
     */
    private interface Transferer
    {
        /**
         * Perform a transfer using the specified transfer manager.
         *
         * @param transferManager the transfer manager.
         *
         * @return the transfer information for the transfer. This will typically be returned from an operation on the transfer manager (e.g. upload).
         */
        public Transfer performTransfer(TransferManager transferManager);
    }

    /**
     * A {@link AWSCredentialsProvider} which delegates to its wrapped {@link HerdAWSCredentialsProvider}
     */
    private static class HerdAwsCredentialsProviderWrapper implements AWSCredentialsProvider
    {
        private HerdAWSCredentialsProvider herdAWSCredentialsProvider;

        public HerdAwsCredentialsProviderWrapper(HerdAWSCredentialsProvider herdAWSCredentialsProvider)
        {
            this.herdAWSCredentialsProvider = herdAWSCredentialsProvider;
        }

        @Override
        public AWSCredentials getCredentials()
        {
            AwsCredential herdAwsCredential = herdAWSCredentialsProvider.getAwsCredential();
            return new BasicSessionCredentials(herdAwsCredential.getAwsAccessKey(), herdAwsCredential.getAwsSecretKey(),
                herdAwsCredential.getAwsSessionToken());
        }

        @Override
        public void refresh()
        {
            // No need to implement this. AWS doesn't use this.
        }
    }
}
