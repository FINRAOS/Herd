﻿/*
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

--
-- Data for Name: actn_type_cd_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO actn_type_cd_lk VALUES ('JOB', 'Job', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');

--
-- Data for Name: bus_objct_data_stts_cd_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO bus_objct_data_stts_cd_lk VALUES ('EXPIRED', 'Expired', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO bus_objct_data_stts_cd_lk VALUES ('INVALID', 'Invalid', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO bus_objct_data_stts_cd_lk VALUES ('ARCHIVED', 'Archived', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO bus_objct_data_stts_cd_lk VALUES ('VALID', 'Valid', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO bus_objct_data_stts_cd_lk VALUES ('UPLOADING', 'Uploading', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO bus_objct_data_stts_cd_lk VALUES ('RE-ENCRYPTING', 'RE-ENCRYPTING', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO bus_objct_data_stts_cd_lk VALUES ('DELETED', 'Deleted', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');

--
-- Data for Name: file_type_cd_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO file_type_cd_lk VALUES ('BZ', 'BZIP2 compressed data', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO file_type_cd_lk VALUES ('GZ', 'GNU Zip file', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO file_type_cd_lk VALUES ('ORC', 'Optimized Row Columnar file', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO file_type_cd_lk VALUES ('TXT', 'Text file', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');


--
-- Data for Name: ntfcn_event_type_cd_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO ntfcn_event_type_cd_lk VALUES ('BUS_OBJCT_DATA_RGSTN', 'Business Object Data Registration', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ntfcn_event_type_cd_lk VALUES ('BUS_OBJCT_DATA_STTS_CHG', 'Business Object Data Status Change', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');



--
-- Data for Name: ntfcn_type_cd_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO ntfcn_type_cd_lk VALUES ('BUS_OBJCT_DATA', 'Business Object Data', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');


--
-- Data for Name: strge_pltfm; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO strge_pltfm VALUES ('S3', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');

--
-- Data for Name: strge; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO strge VALUES ('S3_MANAGED', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM', 'S3');
INSERT INTO strge VALUES ('S3_MANAGED_EXTERNAL', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM', 'S3');
INSERT INTO strge VALUES ('S3_MANAGED_LOADING_DOCK', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM', 'S3');


--
-- Data for Name: ec2_od_prcng_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--

INSERT INTO ec2_od_prcng_lk VALUES (1, 'us-east-1', 't2.micro', 0.013, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (2, 'us-east-1', 't2.small', 0.026, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (3, 'us-east-1', 't2.medium', 0.052, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (4, 'us-east-1', 't2.large', 0.104, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (5, 'us-east-1', 'm4.large', 0.126, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (6, 'us-east-1', 'm4.xlarge', 0.252, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (7, 'us-east-1', 'm4.2xlarge', 0.504, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (8, 'us-east-1', 'm4.4xlarge', 1.008, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (9, 'us-east-1', 'm4.10xlarge', 2.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (10, 'us-east-1', 'm3.medium', 0.067, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (11, 'us-east-1', 'm3.large', 0.133, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (12, 'us-east-1', 'm3.xlarge', 0.266, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (13, 'us-east-1', 'm3.2xlarge', 0.532, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (14, 'us-east-1', 'c4.large', 0.11, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (15, 'us-east-1', 'c4.xlarge', 0.22, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (16, 'us-east-1', 'c4.2xlarge', 0.441, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (17, 'us-east-1', 'c4.4xlarge', 0.882, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (18, 'us-east-1', 'c4.8xlarge', 1.763, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (19, 'us-east-1', 'c3.large', 0.105, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (20, 'us-east-1', 'c3.xlarge', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (21, 'us-east-1', 'c3.2xlarge', 0.42, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (22, 'us-east-1', 'c3.4xlarge', 0.84, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (23, 'us-east-1', 'c3.8xlarge', 1.68, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (24, 'us-east-1', 'g2.2xlarge', 0.65, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (25, 'us-east-1', 'g2.8xlarge', 2.6, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (26, 'us-east-1', 'r3.large', 0.175, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (27, 'us-east-1', 'r3.xlarge', 0.35, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (28, 'us-east-1', 'r3.2xlarge', 0.7, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (29, 'us-east-1', 'r3.4xlarge', 1.4, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (30, 'us-east-1', 'r3.8xlarge', 2.8, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (31, 'us-east-1', 'i2.xlarge', 0.853, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (32, 'us-east-1', 'i2.2xlarge', 1.705, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (33, 'us-east-1', 'i2.4xlarge', 3.41, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (34, 'us-east-1', 'i2.8xlarge', 6.82, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (35, 'us-east-1', 'd2.xlarge', 0.69, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (36, 'us-east-1', 'd2.2xlarge', 1.38, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (37, 'us-east-1', 'd2.4xlarge', 2.76, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (38, 'us-east-1', 'd2.8xlarge', 5.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (39, 'us-west-2', 't2.micro', 0.013, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (40, 'us-west-2', 't2.small', 0.026, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (41, 'us-west-2', 't2.medium', 0.052, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (42, 'us-west-2', 't2.large', 0.104, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (43, 'us-west-2', 'm4.large', 0.126, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (44, 'us-west-2', 'm4.xlarge', 0.252, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (45, 'us-west-2', 'm4.2xlarge', 0.504, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (46, 'us-west-2', 'm4.4xlarge', 1.008, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (47, 'us-west-2', 'm4.10xlarge', 2.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (48, 'us-west-2', 'm3.medium', 0.067, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (49, 'us-west-2', 'm3.large', 0.133, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (50, 'us-west-2', 'm3.xlarge', 0.266, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (51, 'us-west-2', 'm3.2xlarge', 0.532, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (52, 'us-west-2', 'c4.large', 0.11, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (53, 'us-west-2', 'c4.xlarge', 0.22, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (54, 'us-west-2', 'c4.2xlarge', 0.441, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (55, 'us-west-2', 'c4.4xlarge', 0.882, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (56, 'us-west-2', 'c4.8xlarge', 1.763, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (57, 'us-west-2', 'c3.large', 0.105, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (58, 'us-west-2', 'c3.xlarge', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (59, 'us-west-2', 'c3.2xlarge', 0.42, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (60, 'us-west-2', 'c3.4xlarge', 0.84, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (61, 'us-west-2', 'c3.8xlarge', 1.68, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (62, 'us-west-2', 'g2.2xlarge', 0.65, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (63, 'us-west-2', 'g2.8xlarge', 2.6, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (64, 'us-west-2', 'r3.large', 0.175, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (65, 'us-west-2', 'r3.xlarge', 0.35, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (66, 'us-west-2', 'r3.2xlarge', 0.7, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (67, 'us-west-2', 'r3.4xlarge', 1.4, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (68, 'us-west-2', 'r3.8xlarge', 2.8, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (69, 'us-west-2', 'i2.xlarge', 0.853, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (70, 'us-west-2', 'i2.2xlarge', 1.705, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (71, 'us-west-2', 'i2.4xlarge', 3.41, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (72, 'us-west-2', 'i2.8xlarge', 6.82, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (73, 'us-west-2', 'd2.xlarge', 0.69, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (74, 'us-west-2', 'd2.2xlarge', 1.38, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (75, 'us-west-2', 'd2.4xlarge', 2.76, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (76, 'us-west-2', 'd2.8xlarge', 5.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (77, 'us-west-1', 't2.micro', 0.017, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (78, 'us-west-1', 't2.small', 0.034, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (79, 'us-west-1', 't2.medium', 0.068, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (80, 'us-west-1', 't2.large', 0.136, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (81, 'us-west-1', 'm4.large', 0.147, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (82, 'us-west-1', 'm4.xlarge', 0.294, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (83, 'us-west-1', 'm4.2xlarge', 0.588, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (84, 'us-west-1', 'm4.4xlarge', 1.176, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (85, 'us-west-1', 'm4.10xlarge', 2.94, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (86, 'us-west-1', 'm3.medium', 0.077, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (87, 'us-west-1', 'm3.large', 0.154, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (88, 'us-west-1', 'm3.xlarge', 0.308, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (89, 'us-west-1', 'm3.2xlarge', 0.616, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (90, 'us-west-1', 'c4.large', 0.138, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (91, 'us-west-1', 'c4.xlarge', 0.276, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (92, 'us-west-1', 'c4.2xlarge', 0.552, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (93, 'us-west-1', 'c4.4xlarge', 1.104, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (94, 'us-west-1', 'c4.8xlarge', 2.208, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (95, 'us-west-1', 'c3.large', 0.12, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (96, 'us-west-1', 'c3.xlarge', 0.239, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (97, 'us-west-1', 'c3.2xlarge', 0.478, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (98, 'us-west-1', 'c3.4xlarge', 0.956, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (99, 'us-west-1', 'c3.8xlarge', 1.912, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (100, 'us-west-1', 'g2.2xlarge', 0.702, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (101, 'us-west-1', 'g2.8xlarge', 2.808, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (102, 'us-west-1', 'r3.large', 0.195, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (103, 'us-west-1', 'r3.xlarge', 0.39, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (104, 'us-west-1', 'r3.2xlarge', 0.78, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (105, 'us-west-1', 'r3.4xlarge', 1.56, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (106, 'us-west-1', 'r3.8xlarge', 3.12, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (107, 'us-west-1', 'i2.xlarge', 0.938, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (108, 'us-west-1', 'i2.2xlarge', 1.876, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (109, 'us-west-1', 'i2.4xlarge', 3.751, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (110, 'us-west-1', 'i2.8xlarge', 7.502, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (111, 'eu-west-1', 't2.micro', 0.014, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (112, 'eu-west-1', 't2.small', 0.028, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (113, 'eu-west-1', 't2.medium', 0.056, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (114, 'eu-west-1', 't2.large', 0.112, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (115, 'eu-west-1', 'm4.large', 0.139, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (116, 'eu-west-1', 'm4.xlarge', 0.278, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (117, 'eu-west-1', 'm4.2xlarge', 0.556, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (118, 'eu-west-1', 'm4.4xlarge', 1.112, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (119, 'eu-west-1', 'm4.10xlarge', 2.78, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (120, 'eu-west-1', 'm3.medium', 0.073, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (121, 'eu-west-1', 'm3.large', 0.146, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (122, 'eu-west-1', 'm3.xlarge', 0.293, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (123, 'eu-west-1', 'm3.2xlarge', 0.585, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (124, 'eu-west-1', 'c4.large', 0.125, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (125, 'eu-west-1', 'c4.xlarge', 0.251, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (126, 'eu-west-1', 'c4.2xlarge', 0.502, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (127, 'eu-west-1', 'c4.4xlarge', 1.003, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (128, 'eu-west-1', 'c4.8xlarge', 2.006, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (129, 'eu-west-1', 'c3.large', 0.12, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (130, 'eu-west-1', 'c3.xlarge', 0.239, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (131, 'eu-west-1', 'c3.2xlarge', 0.478, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (132, 'eu-west-1', 'c3.4xlarge', 0.956, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (133, 'eu-west-1', 'c3.8xlarge', 1.912, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (134, 'eu-west-1', 'g2.2xlarge', 0.702, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (135, 'eu-west-1', 'g2.8xlarge', 2.808, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (136, 'eu-west-1', 'r3.large', 0.195, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (137, 'eu-west-1', 'r3.xlarge', 0.39, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (138, 'eu-west-1', 'r3.2xlarge', 0.78, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (139, 'eu-west-1', 'r3.4xlarge', 1.56, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (140, 'eu-west-1', 'r3.8xlarge', 3.12, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (141, 'eu-west-1', 'i2.xlarge', 0.938, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (142, 'eu-west-1', 'i2.2xlarge', 1.876, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (143, 'eu-west-1', 'i2.4xlarge', 3.751, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (144, 'eu-west-1', 'i2.8xlarge', 7.502, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (145, 'eu-west-1', 'd2.xlarge', 0.735, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (146, 'eu-west-1', 'd2.2xlarge', 1.47, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (147, 'eu-west-1', 'd2.4xlarge', 2.94, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (148, 'eu-west-1', 'd2.8xlarge', 5.88, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (149, 'eu-central-1', 't2.micro', 0.015, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (150, 'eu-central-1', 't2.small', 0.03, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (151, 'eu-central-1', 't2.medium', 0.06, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (152, 'eu-central-1', 't2.large', 0.12, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (153, 'eu-central-1', 'm4.large', 0.15, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (154, 'eu-central-1', 'm4.xlarge', 0.3, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (155, 'eu-central-1', 'm4.2xlarge', 0.6, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (156, 'eu-central-1', 'm4.4xlarge', 1.2, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (157, 'eu-central-1', 'm4.10xlarge', 3, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (158, 'eu-central-1', 'm3.medium', 0.079, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (159, 'eu-central-1', 'm3.large', 0.158, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (160, 'eu-central-1', 'm3.xlarge', 0.315, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (161, 'eu-central-1', 'm3.2xlarge', 0.632, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (162, 'eu-central-1', 'c4.large', 0.141, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (163, 'eu-central-1', 'c4.xlarge', 0.281, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (164, 'eu-central-1', 'c4.2xlarge', 0.562, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (165, 'eu-central-1', 'c4.4xlarge', 1.125, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (166, 'eu-central-1', 'c4.8xlarge', 2.25, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (167, 'eu-central-1', 'c3.large', 0.129, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (168, 'eu-central-1', 'c3.xlarge', 0.258, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (169, 'eu-central-1', 'c3.2xlarge', 0.516, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (170, 'eu-central-1', 'c3.4xlarge', 1.032, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (171, 'eu-central-1', 'c3.8xlarge', 2.064, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (172, 'eu-central-1', 'g2.2xlarge', 0.772, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (173, 'eu-central-1', 'g2.8xlarge', 3.088, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (174, 'eu-central-1', 'r3.large', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (175, 'eu-central-1', 'r3.xlarge', 0.421, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (176, 'eu-central-1', 'r3.2xlarge', 0.842, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (177, 'eu-central-1', 'r3.4xlarge', 1.684, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (178, 'eu-central-1', 'r3.8xlarge', 3.369, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (179, 'eu-central-1', 'i2.xlarge', 1.013, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (180, 'eu-central-1', 'i2.2xlarge', 2.026, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (181, 'eu-central-1', 'i2.4xlarge', 4.051, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (182, 'eu-central-1', 'i2.8xlarge', 8.102, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (183, 'eu-central-1', 'd2.xlarge', 0.794, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (184, 'eu-central-1', 'd2.2xlarge', 1.588, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (185, 'eu-central-1', 'd2.4xlarge', 3.176, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (186, 'eu-central-1', 'd2.8xlarge', 6.352, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (187, 'ap-southeast-1', 't2.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (188, 'ap-southeast-1', 't2.small', 0.04, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (189, 'ap-southeast-1', 't2.medium', 0.08, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (190, 'ap-southeast-1', 't2.large', 0.16, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (191, 'ap-southeast-1', 'm4.large', 0.187, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (192, 'ap-southeast-1', 'm4.xlarge', 0.374, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (193, 'ap-southeast-1', 'm4.2xlarge', 0.748, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (194, 'ap-southeast-1', 'm4.4xlarge', 1.496, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (195, 'ap-southeast-1', 'm4.10xlarge', 3.74, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (196, 'ap-southeast-1', 'm3.medium', 0.098, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (197, 'ap-southeast-1', 'm3.large', 0.196, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (198, 'ap-southeast-1', 'm3.xlarge', 0.392, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (199, 'ap-southeast-1', 'm3.2xlarge', 0.784, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (200, 'ap-southeast-1', 'c4.large', 0.152, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (201, 'ap-southeast-1', 'c4.xlarge', 0.304, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (202, 'ap-southeast-1', 'c4.2xlarge', 0.608, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (203, 'ap-southeast-1', 'c4.4xlarge', 1.216, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (204, 'ap-southeast-1', 'c4.8xlarge', 2.432, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (205, 'ap-southeast-1', 'c3.large', 0.132, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (206, 'ap-southeast-1', 'c3.xlarge', 0.265, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (207, 'ap-southeast-1', 'c3.2xlarge', 0.529, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (208, 'ap-southeast-1', 'c3.4xlarge', 1.058, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (209, 'ap-southeast-1', 'c3.8xlarge', 2.117, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (210, 'ap-southeast-1', 'g2.2xlarge', 1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (211, 'ap-southeast-1', 'g2.8xlarge', 4, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (212, 'ap-southeast-1', 'r3.large', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (213, 'ap-southeast-1', 'r3.xlarge', 0.42, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (214, 'ap-southeast-1', 'r3.2xlarge', 0.84, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (215, 'ap-southeast-1', 'r3.4xlarge', 1.68, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (216, 'ap-southeast-1', 'r3.8xlarge', 3.36, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (217, 'ap-southeast-1', 'i2.xlarge', 1.018, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (218, 'ap-southeast-1', 'i2.2xlarge', 2.035, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (219, 'ap-southeast-1', 'i2.4xlarge', 4.07, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (220, 'ap-southeast-1', 'i2.8xlarge', 8.14, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (221, 'ap-southeast-1', 'd2.xlarge', 0.87, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (222, 'ap-southeast-1', 'd2.2xlarge', 1.74, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (223, 'ap-southeast-1', 'd2.4xlarge', 3.48, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (224, 'ap-southeast-1', 'd2.8xlarge', 6.96, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (225, 'ap-northeast-1', 't2.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (226, 'ap-northeast-1', 't2.small', 0.04, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (445, 'us-gov-west-1', 'm2.xlarge', 0.293, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (446, 'us-gov-west-1', 'm2.2xlarge', 0.586, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (447, 'us-gov-west-1', 'm2.4xlarge', 1.171, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (448, 'us-gov-west-1', 'hs1.8xlarge', 5.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (449, 'us-gov-west-1', 't1.micro', 0.024, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (227, 'ap-northeast-1', 't2.medium', 0.08, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (228, 'ap-northeast-1', 't2.large', 0.16, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (229, 'ap-northeast-1', 'm4.large', 0.183, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (230, 'ap-northeast-1', 'm4.xlarge', 0.366, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (231, 'ap-northeast-1', 'm4.2xlarge', 0.732, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (232, 'ap-northeast-1', 'm4.4xlarge', 1.464, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (233, 'ap-northeast-1', 'm4.10xlarge', 3.66, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (234, 'ap-northeast-1', 'm3.medium', 0.096, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (235, 'ap-northeast-1', 'm3.large', 0.193, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (236, 'ap-northeast-1', 'm3.xlarge', 0.385, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (237, 'ap-northeast-1', 'm3.2xlarge', 0.77, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (238, 'ap-northeast-1', 'c4.large', 0.14, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (239, 'ap-northeast-1', 'c4.xlarge', 0.279, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (240, 'ap-northeast-1', 'c4.2xlarge', 0.559, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (241, 'ap-northeast-1', 'c4.4xlarge', 1.117, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (242, 'ap-northeast-1', 'c4.8xlarge', 2.234, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (243, 'ap-northeast-1', 'c3.large', 0.128, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (244, 'ap-northeast-1', 'c3.xlarge', 0.255, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (245, 'ap-northeast-1', 'c3.2xlarge', 0.511, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (246, 'ap-northeast-1', 'c3.4xlarge', 1.021, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (247, 'ap-northeast-1', 'c3.8xlarge', 2.043, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (248, 'ap-northeast-1', 'g2.2xlarge', 0.898, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (249, 'ap-northeast-1', 'g2.8xlarge', 3.592, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (250, 'ap-northeast-1', 'r3.large', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (251, 'ap-northeast-1', 'r3.xlarge', 0.42, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (252, 'ap-northeast-1', 'r3.2xlarge', 0.84, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (253, 'ap-northeast-1', 'r3.4xlarge', 1.68, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (254, 'ap-northeast-1', 'r3.8xlarge', 3.36, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (255, 'ap-northeast-1', 'i2.xlarge', 1.001, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (256, 'ap-northeast-1', 'i2.2xlarge', 2.001, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (257, 'ap-northeast-1', 'i2.4xlarge', 4.002, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (258, 'ap-northeast-1', 'i2.8xlarge', 8.004, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (259, 'ap-northeast-1', 'd2.xlarge', 0.844, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (260, 'ap-northeast-1', 'd2.2xlarge', 1.688, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (261, 'ap-northeast-1', 'd2.4xlarge', 3.376, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (262, 'ap-northeast-1', 'd2.8xlarge', 6.752, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (263, 'ap-southeast-2', 't2.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (264, 'ap-southeast-2', 't2.small', 0.04, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (265, 'ap-southeast-2', 't2.medium', 0.08, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (266, 'ap-southeast-2', 't2.large', 0.16, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (267, 'ap-southeast-2', 'm4.large', 0.177, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (268, 'ap-southeast-2', 'm4.xlarge', 0.354, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (269, 'ap-southeast-2', 'm4.2xlarge', 0.708, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (270, 'ap-southeast-2', 'm4.4xlarge', 1.416, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (271, 'ap-southeast-2', 'm4.10xlarge', 3.54, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (272, 'ap-southeast-2', 'm3.medium', 0.093, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (273, 'ap-southeast-2', 'm3.large', 0.186, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (274, 'ap-southeast-2', 'm3.xlarge', 0.372, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (275, 'ap-southeast-2', 'm3.2xlarge', 0.745, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (276, 'ap-southeast-2', 'c4.large', 0.144, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (277, 'ap-southeast-2', 'c4.xlarge', 0.289, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (278, 'ap-southeast-2', 'c4.2xlarge', 0.578, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (279, 'ap-southeast-2', 'c4.4xlarge', 1.155, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (280, 'ap-southeast-2', 'c4.8xlarge', 2.31, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (281, 'ap-southeast-2', 'c3.large', 0.132, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (282, 'ap-southeast-2', 'c3.xlarge', 0.265, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (283, 'ap-southeast-2', 'c3.2xlarge', 0.529, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (284, 'ap-southeast-2', 'c3.4xlarge', 1.058, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (285, 'ap-southeast-2', 'c3.8xlarge', 2.117, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (286, 'ap-southeast-2', 'g2.2xlarge', 0.898, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (287, 'ap-southeast-2', 'g2.8xlarge', 3.592, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (288, 'ap-southeast-2', 'r3.large', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (289, 'ap-southeast-2', 'r3.xlarge', 0.42, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (290, 'ap-southeast-2', 'r3.2xlarge', 0.84, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (291, 'ap-southeast-2', 'r3.4xlarge', 1.68, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (292, 'ap-southeast-2', 'r3.8xlarge', 3.36, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (293, 'ap-southeast-2', 'i2.xlarge', 1.018, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (294, 'ap-southeast-2', 'i2.2xlarge', 2.035, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (295, 'ap-southeast-2', 'i2.4xlarge', 4.07, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (296, 'ap-southeast-2', 'i2.8xlarge', 8.14, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (297, 'ap-southeast-2', 'd2.xlarge', 0.87, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (298, 'ap-southeast-2', 'd2.2xlarge', 1.74, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (299, 'ap-southeast-2', 'd2.4xlarge', 3.48, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (300, 'ap-southeast-2', 'd2.8xlarge', 6.96, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (301, 'sa-east-1', 't2.micro', 0.027, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (302, 'sa-east-1', 't2.small', 0.054, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (303, 'sa-east-1', 't2.medium', 0.108, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (304, 'sa-east-1', 't2.large', 0.216, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (305, 'sa-east-1', 'm3.medium', 0.095, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (306, 'sa-east-1', 'm3.large', 0.19, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (307, 'sa-east-1', 'm3.xlarge', 0.381, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (308, 'sa-east-1', 'm3.2xlarge', 0.761, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (309, 'sa-east-1', 'c3.large', 0.163, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (310, 'sa-east-1', 'c3.xlarge', 0.325, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (311, 'sa-east-1', 'c3.2xlarge', 0.65, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (312, 'sa-east-1', 'c3.4xlarge', 1.3, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (313, 'sa-east-1', 'c3.8xlarge', 2.6, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (314, 'sa-east-1', 'r3.4xlarge', 2.946, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (315, 'sa-east-1', 'r3.8xlarge', 5.892, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (316, 'us-gov-west-1', 't2.micro', 0.015, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (317, 'us-gov-west-1', 't2.small', 0.031, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (318, 'us-gov-west-1', 't2.medium', 0.062, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (319, 'us-gov-west-1', 't2.large', 0.124, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (320, 'us-gov-west-1', 'm3.medium', 0.084, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (321, 'us-gov-west-1', 'm3.large', 0.168, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (322, 'us-gov-west-1', 'm3.xlarge', 0.336, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (323, 'us-gov-west-1', 'm3.2xlarge', 0.672, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (324, 'us-gov-west-1', 'c3.large', 0.126, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (325, 'us-gov-west-1', 'c3.xlarge', 0.252, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (326, 'us-gov-west-1', 'c3.2xlarge', 0.504, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (327, 'us-gov-west-1', 'c3.4xlarge', 1.008, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (328, 'us-gov-west-1', 'c3.8xlarge', 2.016, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (329, 'us-gov-west-1', 'r3.large', 0.21, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (330, 'us-gov-west-1', 'r3.xlarge', 0.42, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (331, 'us-gov-west-1', 'r3.2xlarge', 0.84, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (332, 'us-gov-west-1', 'r3.4xlarge', 1.68, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (333, 'us-gov-west-1', 'r3.8xlarge', 3.36, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (334, 'us-gov-west-1', 'i2.xlarge', 1.023, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (335, 'us-gov-west-1', 'i2.2xlarge', 2.046, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (336, 'us-gov-west-1', 'i2.4xlarge', 4.092, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (337, 'us-gov-west-1', 'i2.8xlarge', 8.184, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (338, 'us-east-1', 'm1.small', 0.044, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (339, 'us-east-1', 'm1.medium', 0.087, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (340, 'us-east-1', 'm1.large', 0.175, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (341, 'us-east-1', 'm1.xlarge', 0.35, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (342, 'us-east-1', 'c1.medium', 0.13, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (343, 'us-east-1', 'c1.xlarge', 0.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (344, 'us-east-1', 'cc2.8xlarge', 2, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (345, 'us-east-1', 'cg1.4xlarge', 2.1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (346, 'us-east-1', 'm2.xlarge', 0.245, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (347, 'us-east-1', 'm2.2xlarge', 0.49, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (348, 'us-east-1', 'm2.4xlarge', 0.98, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (349, 'us-east-1', 'cr1.8xlarge', 3.5, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (350, 'us-east-1', 'hi1.4xlarge', 3.1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (351, 'us-east-1', 'hs1.8xlarge', 4.6, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (352, 'us-east-1', 't1.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (353, 'us-west-2', 'm1.small', 0.044, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (354, 'us-west-2', 'm1.medium', 0.087, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (355, 'us-west-2', 'm1.large', 0.175, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (356, 'us-west-2', 'm1.xlarge', 0.35, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (357, 'us-west-2', 'c1.medium', 0.13, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (358, 'us-west-2', 'c1.xlarge', 0.52, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (359, 'us-west-2', 'cc2.8xlarge', 2, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (360, 'us-west-2', 'm2.xlarge', 0.245, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (361, 'us-west-2', 'm2.2xlarge', 0.49, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (362, 'us-west-2', 'm2.4xlarge', 0.98, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (363, 'us-west-2', 'cr1.8xlarge', 3.5, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (364, 'us-west-2', 'hi1.4xlarge', 3.1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (365, 'us-west-2', 'hs1.8xlarge', 4.6, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (366, 'us-west-2', 't1.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (367, 'us-west-1', 'm1.small', 0.047, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (368, 'us-west-1', 'm1.medium', 0.095, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (369, 'us-west-1', 'm1.large', 0.19, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (370, 'us-west-1', 'm1.xlarge', 0.379, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (371, 'us-west-1', 'c1.medium', 0.148, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (372, 'us-west-1', 'c1.xlarge', 0.592, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (373, 'us-west-1', 'm2.xlarge', 0.275, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (374, 'us-west-1', 'm2.2xlarge', 0.55, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (375, 'us-west-1', 'm2.4xlarge', 1.1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (376, 'us-west-1', 't1.micro', 0.025, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (377, 'eu-west-1', 'm1.small', 0.047, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (378, 'eu-west-1', 'm1.medium', 0.095, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (379, 'eu-west-1', 'm1.large', 0.19, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (380, 'eu-west-1', 'm1.xlarge', 0.379, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (381, 'eu-west-1', 'c1.medium', 0.148, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (382, 'eu-west-1', 'c1.xlarge', 0.592, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (383, 'eu-west-1', 'cc2.8xlarge', 2.25, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (384, 'eu-west-1', 'cg1.4xlarge', 2.36, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (385, 'eu-west-1', 'm2.xlarge', 0.275, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (386, 'eu-west-1', 'm2.2xlarge', 0.55, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (387, 'eu-west-1', 'm2.4xlarge', 1.1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (388, 'eu-west-1', 'cr1.8xlarge', 3.75, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (389, 'eu-west-1', 'hi1.4xlarge', 3.1, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (390, 'eu-west-1', 'hs1.8xlarge', 4.9, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (391, 'eu-west-1', 't1.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (392, 'ap-southeast-1', 'm1.small', 0.058, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (393, 'ap-southeast-1', 'm1.medium', 0.117, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (394, 'ap-southeast-1', 'm1.large', 0.233, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (395, 'ap-southeast-1', 'm1.xlarge', 0.467, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (396, 'ap-southeast-1', 'c1.medium', 0.164, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (397, 'ap-southeast-1', 'c1.xlarge', 0.655, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (398, 'ap-southeast-1', 'm2.xlarge', 0.296, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (399, 'ap-southeast-1', 'm2.2xlarge', 0.592, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (400, 'ap-southeast-1', 'm2.4xlarge', 1.183, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (401, 'ap-southeast-1', 'hs1.8xlarge', 5.57, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (402, 'ap-southeast-1', 't1.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (403, 'ap-northeast-1', 'm1.small', 0.061, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (404, 'ap-northeast-1', 'm1.medium', 0.122, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (405, 'ap-northeast-1', 'm1.large', 0.243, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (406, 'ap-northeast-1', 'm1.xlarge', 0.486, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (407, 'ap-northeast-1', 'c1.medium', 0.158, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (408, 'ap-northeast-1', 'c1.xlarge', 0.632, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (409, 'ap-northeast-1', 'cc2.8xlarge', 2.349, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (410, 'ap-northeast-1', 'm2.xlarge', 0.287, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (411, 'ap-northeast-1', 'm2.2xlarge', 0.575, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (412, 'ap-northeast-1', 'm2.4xlarge', 1.15, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (413, 'ap-northeast-1', 'cr1.8xlarge', 4.105, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (414, 'ap-northeast-1', 'hi1.4xlarge', 3.276, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (415, 'ap-northeast-1', 'hs1.8xlarge', 5.4, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (416, 'ap-northeast-1', 't1.micro', 0.026, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (417, 'ap-southeast-2', 'm1.small', 0.058, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (418, 'ap-southeast-2', 'm1.medium', 0.117, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (419, 'ap-southeast-2', 'm1.large', 0.233, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (420, 'ap-southeast-2', 'm1.xlarge', 0.467, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (421, 'ap-southeast-2', 'c1.medium', 0.164, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (422, 'ap-southeast-2', 'c1.xlarge', 0.655, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (423, 'ap-southeast-2', 'm2.xlarge', 0.296, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (424, 'ap-southeast-2', 'm2.2xlarge', 0.592, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (425, 'ap-southeast-2', 'm2.4xlarge', 1.183, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (426, 'ap-southeast-2', 'hs1.8xlarge', 5.57, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (427, 'ap-southeast-2', 't1.micro', 0.02, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (428, 'sa-east-1', 'm1.small', 0.058, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (429, 'sa-east-1', 'm1.medium', 0.117, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (430, 'sa-east-1', 'm1.large', 0.233, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (431, 'sa-east-1', 'm1.xlarge', 0.467, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (432, 'sa-east-1', 'c1.medium', 0.179, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (433, 'sa-east-1', 'c1.xlarge', 0.718, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (434, 'sa-east-1', 'm2.xlarge', 0.323, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (435, 'sa-east-1', 'm2.2xlarge', 0.645, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (436, 'sa-east-1', 'm2.4xlarge', 1.291, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (437, 'sa-east-1', 't1.micro', 0.027, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (438, 'us-gov-west-1', 'm1.small', 0.053, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (439, 'us-gov-west-1', 'm1.medium', 0.106, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (440, 'us-gov-west-1', 'm1.large', 0.211, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (441, 'us-gov-west-1', 'm1.xlarge', 0.423, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (442, 'us-gov-west-1', 'c1.medium', 0.157, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (443, 'us-gov-west-1', 'c1.xlarge', 0.628, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO ec2_od_prcng_lk VALUES (444, 'us-gov-west-1', 'cc2.8xlarge', 2.25, current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');

--
-- Data for Name: scrty_fn_lk; Type: TABLE DATA; Schema: dmrowner; Owner: dmrowner
--
INSERT INTO scrty_fn_lk VALUES('FN_BUILD_INFO_GET','FN_BUILD_INFO_GET','FN_BUILD_INFO_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_ALL_GET','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_ALL_GET','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_DELETE','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_DELETE','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_GET','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_GET','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_POST','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_POST','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_PUT','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_PUT','FN_BUSINESS_OBJECT_DATA_ATTRIBUTES_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_AVAILABILITY_COLLECTION_POST','FN_BUSINESS_OBJECT_DATA_AVAILABILITY_COLLECTION_POST','FN_BUSINESS_OBJECT_DATA_AVAILABILITY_COLLECTION_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_AVAILABILITY_POST','FN_BUSINESS_OBJECT_DATA_AVAILABILITY_POST','FN_BUSINESS_OBJECT_DATA_AVAILABILITY_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_DELETE','FN_BUSINESS_OBJECT_DATA_DELETE','FN_BUSINESS_OBJECT_DATA_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_GENERATE_DDL_COLLECTION_POST','FN_BUSINESS_OBJECT_DATA_GENERATE_DDL_COLLECTION_POST','FN_BUSINESS_OBJECT_DATA_GENERATE_DDL_COLLECTION_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_GENERATE_DDL_POST','FN_BUSINESS_OBJECT_DATA_GENERATE_DDL_POST','FN_BUSINESS_OBJECT_DATA_GENERATE_DDL_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_GET','FN_BUSINESS_OBJECT_DATA_GET','FN_BUSINESS_OBJECT_DATA_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_ALL_GET','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_ALL_GET','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_DELETE','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_DELETE','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_GET','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_GET','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_POST','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_POST','FN_BUSINESS_OBJECT_DATA_NOTIFICATION_REGISTRATIONS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_POST','FN_BUSINESS_OBJECT_DATA_POST','FN_BUSINESS_OBJECT_DATA_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_S3_KEY_PREFIX_GET','FN_BUSINESS_OBJECT_DATA_S3_KEY_PREFIX_GET','FN_BUSINESS_OBJECT_DATA_S3_KEY_PREFIX_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_STATUS_GET','FN_BUSINESS_OBJECT_DATA_STATUS_GET','FN_BUSINESS_OBJECT_DATA_STATUS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_STATUS_PUT','FN_BUSINESS_OBJECT_DATA_STATUS_PUT','FN_BUSINESS_OBJECT_DATA_STATUS_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_STORAGES_FILES_POST','FN_BUSINESS_OBJECT_DATA_STORAGES_FILES_POST','FN_BUSINESS_OBJECT_DATA_STORAGES_FILES_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_UNREGISTERED_INVALIDATE','FN_BUSINESS_OBJECT_DATA_UNREGISTERED_INVALIDATE','FN_BUSINESS_OBJECT_DATA_UNREGISTERED_INVALIDATE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DATA_VERSIONS_GET','FN_BUSINESS_OBJECT_DATA_VERSIONS_GET','FN_BUSINESS_OBJECT_DATA_VERSIONS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DEFINITIONS_ALL_GET','FN_BUSINESS_OBJECT_DEFINITIONS_ALL_GET','FN_BUSINESS_OBJECT_DEFINITIONS_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DEFINITIONS_DELETE','FN_BUSINESS_OBJECT_DEFINITIONS_DELETE','FN_BUSINESS_OBJECT_DEFINITIONS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DEFINITIONS_GET','FN_BUSINESS_OBJECT_DEFINITIONS_GET','FN_BUSINESS_OBJECT_DEFINITIONS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DEFINITIONS_POST','FN_BUSINESS_OBJECT_DEFINITIONS_POST','FN_BUSINESS_OBJECT_DEFINITIONS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_DEFINITIONS_PUT','FN_BUSINESS_OBJECT_DEFINITIONS_PUT','FN_BUSINESS_OBJECT_DEFINITIONS_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_ALL_GET','FN_BUSINESS_OBJECT_FORMATS_ALL_GET','FN_BUSINESS_OBJECT_FORMATS_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_DELETE','FN_BUSINESS_OBJECT_FORMATS_DELETE','FN_BUSINESS_OBJECT_FORMATS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_GENERATE_DDL_COLLECTION_POST','FN_BUSINESS_OBJECT_FORMATS_GENERATE_DDL_COLLECTION_POST','FN_BUSINESS_OBJECT_FORMATS_GENERATE_DDL_COLLECTION_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_GENERATE_DDL_POST','FN_BUSINESS_OBJECT_FORMATS_GENERATE_DDL_POST','FN_BUSINESS_OBJECT_FORMATS_GENERATE_DDL_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_GET','FN_BUSINESS_OBJECT_FORMATS_GET','FN_BUSINESS_OBJECT_FORMATS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_POST','FN_BUSINESS_OBJECT_FORMATS_POST','FN_BUSINESS_OBJECT_FORMATS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_BUSINESS_OBJECT_FORMATS_PUT','FN_BUSINESS_OBJECT_FORMATS_PUT','FN_BUSINESS_OBJECT_FORMATS_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_CUSTOM_DDLS_ALL_GET','FN_CUSTOM_DDLS_ALL_GET','FN_CUSTOM_DDLS_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_CUSTOM_DDLS_DELETE','FN_CUSTOM_DDLS_DELETE','FN_CUSTOM_DDLS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_CUSTOM_DDLS_GET','FN_CUSTOM_DDLS_GET','FN_CUSTOM_DDLS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_CUSTOM_DDLS_POST','FN_CUSTOM_DDLS_POST','FN_CUSTOM_DDLS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_CUSTOM_DDLS_PUT','FN_CUSTOM_DDLS_PUT','FN_CUSTOM_DDLS_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_DISPLAY_DM_UI','FN_DISPLAY_DM_UI','FN_DISPLAY_DM_UI', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_DOWNLOAD_GET','FN_DOWNLOAD_GET','FN_DOWNLOAD_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTERS_DELETE','FN_EMR_CLUSTERS_DELETE','FN_EMR_CLUSTERS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTERS_GET','FN_EMR_CLUSTERS_GET','FN_EMR_CLUSTERS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTERS_POST','FN_EMR_CLUSTERS_POST','FN_EMR_CLUSTERS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTER_DEFINITIONS_DELETE','FN_EMR_CLUSTER_DEFINITIONS_DELETE','FN_EMR_CLUSTER_DEFINITIONS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTER_DEFINITIONS_GET','FN_EMR_CLUSTER_DEFINITIONS_GET','FN_EMR_CLUSTER_DEFINITIONS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTER_DEFINITIONS_POST','FN_EMR_CLUSTER_DEFINITIONS_POST','FN_EMR_CLUSTER_DEFINITIONS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_CLUSTER_DEFINITIONS_PUT','FN_EMR_CLUSTER_DEFINITIONS_PUT','FN_EMR_CLUSTER_DEFINITIONS_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_HADOOP_JAR_STEPS_POST','FN_EMR_HADOOP_JAR_STEPS_POST','FN_EMR_HADOOP_JAR_STEPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_HIVE_STEPS_POST','FN_EMR_HIVE_STEPS_POST','FN_EMR_HIVE_STEPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_MASTER_SECURITY_GROUPS_POST','FN_EMR_MASTER_SECURITY_GROUPS_POST','FN_EMR_MASTER_SECURITY_GROUPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_OOZIE_STEPS_POST','FN_EMR_OOZIE_STEPS_POST','FN_EMR_OOZIE_STEPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_OOZIE_WORKFLOW_GET','FN_EMR_OOZIE_WORKFLOW_GET','FN_EMR_OOZIE_WORKFLOW_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_PIG_STEPS_POST','FN_EMR_PIG_STEPS_POST','FN_EMR_PIG_STEPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EMR_SHELL_STEPS_POST','FN_EMR_SHELL_STEPS_POST','FN_EMR_SHELL_STEPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EXPECTED_PARTITION_VALUES_DELETE','FN_EXPECTED_PARTITION_VALUES_DELETE','FN_EXPECTED_PARTITION_VALUES_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EXPECTED_PARTITION_VALUES_GET','FN_EXPECTED_PARTITION_VALUES_GET','FN_EXPECTED_PARTITION_VALUES_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_EXPECTED_PARTITION_VALUES_POST','FN_EXPECTED_PARTITION_VALUES_POST','FN_EXPECTED_PARTITION_VALUES_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_FILE_TYPES_ALL_GET','FN_FILE_TYPES_ALL_GET','FN_FILE_TYPES_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_JOBS_GET','FN_JOBS_GET','FN_JOBS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_JOBS_POST','FN_JOBS_POST','FN_JOBS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_JOBS_SIGNAL_POST','FN_JOBS_SIGNAL_POST','FN_JOBS_SIGNAL_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_JOB_DEFINITIONS_GET','FN_JOB_DEFINITIONS_GET','FN_JOB_DEFINITIONS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_JOB_DEFINITIONS_POST','FN_JOB_DEFINITIONS_POST','FN_JOB_DEFINITIONS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_JOB_DEFINITIONS_PUT','FN_JOB_DEFINITIONS_PUT','FN_JOB_DEFINITIONS_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_NAMESPACES_ALL_GET','FN_NAMESPACES_ALL_GET','FN_NAMESPACES_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_NAMESPACES_DELETE','FN_NAMESPACES_DELETE','FN_NAMESPACES_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_NAMESPACES_GET','FN_NAMESPACES_GET','FN_NAMESPACES_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_NAMESPACES_POST','FN_NAMESPACES_POST','FN_NAMESPACES_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_PARTITION_KEY_GROUPS_ALL_GET','FN_PARTITION_KEY_GROUPS_ALL_GET','FN_PARTITION_KEY_GROUPS_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_PARTITION_KEY_GROUPS_DELETE','FN_PARTITION_KEY_GROUPS_DELETE','FN_PARTITION_KEY_GROUPS_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_PARTITION_KEY_GROUPS_GET','FN_PARTITION_KEY_GROUPS_GET','FN_PARTITION_KEY_GROUPS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_PARTITION_KEY_GROUPS_POST','FN_PARTITION_KEY_GROUPS_POST','FN_PARTITION_KEY_GROUPS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGES_ALL_GET','FN_STORAGES_ALL_GET','FN_STORAGES_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGES_DELETE','FN_STORAGES_DELETE','FN_STORAGES_DELETE', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGES_GET','FN_STORAGES_GET','FN_STORAGES_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGES_POST','FN_STORAGES_POST','FN_STORAGES_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGES_PUT','FN_STORAGES_PUT','FN_STORAGES_PUT', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGES_UPLOAD_STATS_GET','FN_STORAGES_UPLOAD_STATS_GET','FN_STORAGES_UPLOAD_STATS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGE_PLATFORMS_ALL_GET','FN_STORAGE_PLATFORMS_ALL_GET','FN_STORAGE_PLATFORMS_ALL_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_STORAGE_PLATFORMS_GET','FN_STORAGE_PLATFORMS_GET','FN_STORAGE_PLATFORMS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_SYSTEM_JOBS_POST','FN_SYSTEM_JOBS_POST','FN_SYSTEM_JOBS_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_UPLOAD_EXTEND_CREDENTIALS_GET','FN_UPLOAD_EXTEND_CREDENTIALS_GET','FN_UPLOAD_EXTEND_CREDENTIALS_GET', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
INSERT INTO scrty_fn_lk VALUES('FN_UPLOAD_POST','FN_UPLOAD_POST','FN_UPLOAD_POST', current_timestamp, 'SYSTEM', current_timestamp, 'SYSTEM');
