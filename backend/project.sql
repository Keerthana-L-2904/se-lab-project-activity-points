use student_activity_points;
select * from student_activity;
select * from activity;
select * from admin;
select * from departments;
select * from requests;
SELECT dept_points, institute_points, other_points, activity_points FROM student WHERE sid = 'B220038CS';
select * from fa;
show tables;
SELECT DISTINCT DID FROM fa;
SELECT DID FROM fa WHERE DID NOT IN (SELECT DID FROM departments);



/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity` (
  `actID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text,
  `date` datetime(6) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `mandatory` int DEFAULT NULL,
  `points` int DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `DID` int DEFAULT NULL,
  PRIMARY KEY (`actID`),
  KEY `activity_ibfk_1` (`DID`),
  CONSTRAINT `activity_ibfk_1` FOREIGN KEY (`DID`) REFERENCES `departments` (`DID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (4,'Indian Consititution','fhgfhjjdjjdd','2025-02-12 00:00:00.000000','Department','Inside',1,4,'2024-03-17 00:00:00.000000',1,0),(15,'Volunteering','ddd','2025-04-04 05:30:00.000000','Institute','Inside',0,4,'2025-04-05 05:30:00.000000',1,NULL),(16,'nss','eded','2025-04-02 05:30:00.000000','Institute','Inside',1,4,'2025-04-03 05:30:00.000000',1,NULL);
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'Admin User','admin@example.com','adminpassword'),(2,'KEERTHANA LEKSHMINARYANAN','vaish_b22@nitc.ac.in','vaish');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcements`
--

DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcements` (
  `aid` bigint NOT NULL AUTO_INCREMENT,
  `FAID` int NOT NULL,
  `date` datetime(6) NOT NULL,
  `time` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `body` text,
  PRIMARY KEY (`aid`),
  KEY `FAID` (`FAID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcements`
--

LOCK TABLES `announcements` WRITE;
/*!40000 ALTER TABLE `announcements` DISABLE KEYS */;
INSERT INTO `announcements` VALUES (1,1,'2024-03-17 00:00:00.000000','12:30:45',' FA Meeting 2 Postponement','FA meeting is postponed to 18th March 2024'),(2,3,'2023-12-21 00:00:00.000000','12:30:45','Certificate Upload Deadline','Deadline for Winter semester: 20th April 2024'),(3,3,'2023-08-19 00:00:00.000000','12:30:45','FA Meeting 1','FA Meeting to be held on 21 August 2023');
/*!40000 ALTER TABLE `announcements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `DID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `depname` varchar(255) DEFAULT NULL,
  `dep_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`DID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (1,'Computer Science',NULL,NULL);
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fa`
--

DROP TABLE IF EXISTS `fa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fa` (
  `FAID` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `emailID` varchar(255) NOT NULL,
  `DID` int NOT NULL,
  PRIMARY KEY (`FAID`),
  UNIQUE KEY `emailID` (`emailID`),
  KEY `DID` (`DID`),
  CONSTRAINT `fa_ibfk_1` FOREIGN KEY (`DID`) REFERENCES `departments` (`DID`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fa`
--

LOCK TABLES `fa` WRITE;
/*!40000 ALTER TABLE `fa` DISABLE KEYS */;
INSERT INTO `fa` VALUES (1,'Veda FAa','veda_b220584cs@nitc.ac.in',1),(2,'Veena FA','veenavijayshankar@gmail.com',1),(3,'keer','kandyy2904@gmail.com',1),(4,'rita kumarr','rita@gmail.com',1);
/*!40000 ALTER TABLE `fa` ENABLE KEYS */;
UNLOCK TABLES;

DROP TABLE IF EXISTS `requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `requests` (
  `rid` bigint NOT NULL AUTO_INCREMENT,
  `sid` varchar(255) NOT NULL,
  `date` datetime(6) NOT NULL,
  `status` enum('Pending','Approved','Rejected') NOT NULL DEFAULT 'Pending',
  `link` varchar(255) DEFAULT NULL,
  `decision_date` date DEFAULT NULL,
  `activity_name` varchar(255) DEFAULT NULL,
  `description` text,
  `activity_date` datetime(6) DEFAULT NULL,
  `points` int DEFAULT NULL,
  `type` enum('Institute','Department','other') NOT NULL,
  `decison_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`rid`),
  KEY `sid` (`sid`),
  CONSTRAINT `requests_ibfk_1` FOREIGN KEY (`sid`) REFERENCES `student` (`SID`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `requests`
--

LOCK TABLES `requests` WRITE;
/*!40000 ALTER TABLE `requests` DISABLE KEYS */;
INSERT INTO `requests` VALUES (1,'B220584CS','2024-12-01 00:00:00.000000','Approved','http://example.com/request','2024-12-18','Art Competition','Participated in inter-college art competition','2024-11-28 00:00:00.000000','Institute',NULL),(22,'B220038CS','2025-04-05 11:45:16.653000','Approved','www.example.com',NULL,'Indian Consititution','',NULL,'Department','2025-04-05 06:15:16.643000'),(23,'B220038CS','2025-04-06 19:05:46.027000','Approved','www.example.com',NULL,'Volunteering','',NULL,'Institute','2025-04-06 13:35:46.010000'),(24,'B220038CS','2025-04-06 19:11:21.344000','Rejected','www.example.com',NULL,'nss','',NULL,'Institute','2025-04-06 13:41:21.328000');
/*!40000 ALTER TABLE `requests` ENABLE KEYS */;
UNLOCK TABLES;

-- Drop table if it already exists
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `SID` varchar(255) NOT NULL,
  `FAID` int NOT NULL,
  `emailID` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `DID` int NOT NULL,
  `dept_points` int DEFAULT 0,
  `institute_points` int DEFAULT 0,
  `other_points` int DEFAULT 0,
  `activity_points` int GENERATED ALWAYS AS (`dept_points` + `institute_points` + `other_points`) STORED,
  PRIMARY KEY (`SID`),
  UNIQUE KEY `emailID` (`emailID`),
  KEY `FAID` (`FAID`),
  KEY `DID` (`DID`),
  CONSTRAINT `student_ibfk_1` FOREIGN KEY (`FAID`) REFERENCES `fa` (`FAID`),
  CONSTRAINT `student_ibfk_2` FOREIGN KEY (`DID`) REFERENCES `departments` (`DID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert data (DO NOT include activity_points because it's calculated automatically)
INSERT INTO `student` (`SID`, `FAID`, `emailID`, `name`, `DID`, `dept_points`, `institute_points`, `other_points`) VALUES
('B220038CS', 3, 'keerthana_b220038cs@nitc.ac.in', 'Keerthana', 1, 23, 40, 0),
('B220078EC', 1, 'keer@nitc.ac.in', 'kkkekeke', 1, 14, 25, 0),
('B220448CS', 1, 'berty@gmail.com', 'berty', 1, 11, 16, 0),
('B220584CS', 3, 'vedavijayshankar.xia6@gmail.com', 'Veda Vijay Shankar', 1, 10, 20, 0);

--
-- Table structure for table `student_activity`
--

DROP TABLE IF EXISTS `student_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_activity` (
  `actid` int NOT NULL AUTO_INCREMENT,
  `sid` varchar(255) NOT NULL,
  `date` datetime(6) NOT NULL,
  `activity_type` varchar(255) NOT NULL,
  `points` int DEFAULT '0',
  `title` varchar(255) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`actid`,`sid`),
  KEY `FKowxecsdxbyhk408543vf01hy1` (`sid`),
  CONSTRAINT `FK4oh3s3nc1x37b0e5rsy6n8mkl` FOREIGN KEY (`actid`) REFERENCES `activity` (`actID`),
  CONSTRAINT `FKowxecsdxbyhk408543vf01hy1` FOREIGN KEY (`sid`) REFERENCES `student` (`SID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_activity`
--

LOCK TABLES `student_activity` WRITE;
/*!40000 ALTER TABLE `student_activity` DISABLE KEYS */;
INSERT INTO `student_activity` VALUES (4,'B220038CS','2025-04-05 11:47:44.264000','Department',4,'Indian Consititution','www.example.com'),(4,'B220584CS','2023-02-13 00:00:00.000000','Department',3,'indian constitution','https://docs.google.com/document/d/1jnaaD7Lyo85oa40Rc4BOyAlEg2ZVcuZrVkZLvV1i9c0/edit?tab=t.0'),(15,'B220038CS','2025-04-06 19:10:22.676000','Institute',4,'Volunteering','www.example.com');
/*!40000 ALTER TABLE `student_activity` ENABLE KEYS */;
UNLOCK TABLES;
