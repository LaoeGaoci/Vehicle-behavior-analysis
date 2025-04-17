-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: vba
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `judge`
--

DROP TABLE IF EXISTS `judge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `judge` (
  `video_id` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `risk_level` varchar(255) NOT NULL,
  `selection_difficulty` int NOT NULL,
  PRIMARY KEY (`video_id`,`username`),
  KEY `username` (`username`),
  CONSTRAINT `judge_ibfk_1` FOREIGN KEY (`username`) REFERENCES `user` (`username`),
  CONSTRAINT `judge_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `video` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `judge`
--

LOCK TABLES `judge` WRITE;
/*!40000 ALTER TABLE `judge` DISABLE KEYS */;
INSERT INTO `judge` VALUES ('i-80~1461.mp4','hcy','低风险',2),('i-80~1484.mp4','hcy','中等风险',2),('i-80~1484.mp4','hyl','中等风险',2),('i-80~1561.mp4','hcy','低风险',2),('i-80~1620.mp4','hcy','中等风险',3),('i-80~1706.mp4','hcy','低风险',2),('i-80~1739.mp4','hcy','极度危险',2);
/*!40000 ALTER TABLE `judge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `username` varchar(255) NOT NULL,
  `age` int NOT NULL,
  `driver_years` int NOT NULL,
  `gender` varchar(255) NOT NULL,
  PRIMARY KEY (`username`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('12',14,2,'男'),('ccc',12,12,'男'),('erq',12,12,'男'),('hcy',21,0,'男'),('hyl',12,12,'男');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `video`
--

DROP TABLE IF EXISTS `video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `video` (
  `video_id` varchar(255) NOT NULL,
  `risk_level` varchar(255) NOT NULL,
  `selection_difficulty` int NOT NULL,
  `V1` int NOT NULL,
  `V3` int NOT NULL,
  `V4` int NOT NULL,
  `a1` int NOT NULL,
  `a2` int NOT NULL,
  `a3` int NOT NULL,
  `a4` int NOT NULL,
  `G` int NOT NULL,
  `L2` int NOT NULL,
  `L3` int NOT NULL,
  `L4` int DEFAULT NULL,
  PRIMARY KEY (`video_id`),
  UNIQUE KEY `video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `video`
--

LOCK TABLES `video` WRITE;
/*!40000 ALTER TABLE `video` DISABLE KEYS */;
INSERT INTO `video` VALUES ('i-80~1037.mp4','高风险',1,56,13,14,-11,0,0,-5,68,20,34,35),('i-80~1291.mp4','高风险',1,51,31,29,11,2,-11,-8,61,64,47,15),('i-80~1305.mp4','较高风险',1,29,25,23,1,-4,0,-2,61,43,27,35),('i-80~1355.mp4','较高风险',1,60,49,47,0,-6,-11,11,136,46,42,94),('i-80~1420.mp4','低风险',0,27,25,28,-11,-2,2,-2,128,70,89,39),('i-80~1425.mp4','低风险',0,28,30,26,2,1,-10,11,151,23,67,84),('i-80~1435.mp4','高风险',1,59,19,22,11,-8,-3,-9,70,46,17,53),('i-80~1445.mp4','高风险',1,56,25,21,-11,11,8,-3,58,0,40,17),('i-80~1461.mp4','较高风险',1,33,27,24,-1,0,1,5,65,59,33,31),('i-80~1484.mp4','较高风险',1,33,27,27,10,0,3,0,78,45,35,43),('i-80~1561.mp4','较低风险',0,28,30,25,0,-8,0,-6,96,23,47,48),('i-80~1620.mp4','高风险',1,41,5,14,0,-8,10,4,49,8,30,20),('i-80~1706.mp4','高风险',1,53,8,0,0,0,0,0,28,83,28,0),('i-80~1707.mp4','较高风险',1,53,41,45,0,0,-2,2,120,2,49,70),('i-80~1739.mp4','高风险',1,63,23,10,-9,0,8,0,68,65,13,55),('i-80~711.mp4','高风险',1,59,24,5,0,0,0,0,64,2,63,1),('i-80~734.mp4','较低风险',0,34,36,38,0,0,0,2,98,58,37,61),('i-80~740.mp4','较低风险',0,39,34,40,0,-10,6,7,107,1,60,48),('i-80~836.mp4','高风险',1,51,25,28,0,0,0,0,62,37,4,59),('i-80~837.mp4','较高风险',1,51,73,56,0,0,-4,-4,106,52,68,38),('i-80~844.mp4','低风险',0,45,41,48,-1,-4,0,9,193,8,100,93),('i-80~908.mp4','高风险',1,59,30,28,11,0,0,11,65,65,51,14);
/*!40000 ALTER TABLE `video` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-17 22:35:39
