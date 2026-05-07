-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 28, 2026 at 08:23 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sewa_camping`
CREATE DATABASE IF NOT EXISTS sewa_camping;
USE sewa_camping; 
--

DELIMITER $$
--
-- Functions
--
CREATE DEFINER=`root`@`localhost` FUNCTION `total_pendapatan` () RETURNS INT(11) DETERMINISTIC BEGIN
    DECLARE total INT;
    SELECT SUM(d.jumlah*a.harga*d.hari)
    INTO total
    FROM detail_transaksi d
    JOIN alat a ON d.id_alat=a.id_alat;
    RETURN total;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `alat`
--

CREATE TABLE `alat` (
  `id_alat` varchar(10) NOT NULL,
  `nama_alat` varchar(100) DEFAULT NULL,
  `stok` int(11) DEFAULT NULL,
  `harga` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `alat`
--

INSERT INTO `alat` (`id_alat`, `nama_alat`, `stok`, `harga`) VALUES
('ALT001', 'Tenda Dome 2 Orang', 4, 50000),
('ALT002', 'Tenda Dome 4 Orang', 7, 75000),
('ALT003', 'Sleeping Bag', 20, 20000),
('ALT004', 'Matras Camping', 15, 15000),
('ALT005', 'Kompor Portable', 11, 30000),
('ALT006', 'Gas Kaleng', 25, 10000),
('ALT007', 'Carrier 50L', 7, 40000),
('ALT008', 'Carrier 70L', 5, 50000),
('ALT009', 'Headlamp', 17, 10000),
('ALT010', 'Senter LED', 17, 8000),
('ALT011', 'Flysheet', 6, 25000),
('ALT012', 'Meja Lipat', 5, 30000),
('ALT013', 'Kursi Lipat', 10, 20000),
('ALT014', 'Hammock', 9, 25000),
('ALT015', 'Jas Hujan', 15, 10000),
('ALT016', 'Sepatu Gunung', 8, 35000),
('ALT017', 'Cooking Set', 6, 40000),
('ALT018', 'Water Carrier', 10, 15000),
('ALT019', 'Lampu Camping', 12, 20000),
('ALT020', 'Pisau Lipat', 14, 12000);

-- --------------------------------------------------------

--
-- Table structure for table `detail_transaksi`
--

CREATE TABLE `detail_transaksi` (
  `id_detail` int(11) NOT NULL,
  `id_transaksi` varchar(10) DEFAULT NULL,
  `id_alat` varchar(10) DEFAULT NULL,
  `jumlah` int(11) DEFAULT NULL,
  `hari` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `detail_transaksi`
--

INSERT INTO `detail_transaksi` (`id_detail`, `id_transaksi`, `id_alat`, `jumlah`, `hari`) VALUES
(16, 'TRX0002', 'ALT002', 1, 1),
(17, 'TRX0002', 'ALT003', 1, 1),
(18, 'TRX0003', 'ALT005', 1, 1),
(19, 'TRX0004', 'ALT020', 1, 5);

--
-- Triggers `detail_transaksi`
--
DELIMITER $$
CREATE TRIGGER `kurangi_stok` AFTER INSERT ON `detail_transaksi` FOR EACH ROW BEGIN
    UPDATE alat SET stok = stok - NEW.jumlah
    WHERE id_alat = NEW.id_alat;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `penyewa`
--

CREATE TABLE `penyewa` (
  `id_penyewa` varchar(10) NOT NULL,
  `nama` varchar(100) DEFAULT NULL,
  `no_hp` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `penyewa`
--

INSERT INTO `penyewa` (`id_penyewa`, `nama`, `no_hp`) VALUES
('SW0001', 'Arsyad', '0857'),
('SW0002', 'a', 'q'),
('SW0003', 'A', '1'),
('SW0004', 'Nova', '082244656887');

-- --------------------------------------------------------

--
-- Table structure for table `transaksi`
--

CREATE TABLE `transaksi` (
  `id_transaksi` varchar(10) NOT NULL,
  `id_penyewa` varchar(10) DEFAULT NULL,
  `tanggal_sewa` date DEFAULT NULL,
  `tanggal_kembali` date DEFAULT NULL,
  `tanggal_kembali_real` date DEFAULT NULL,
  `denda` int(11) DEFAULT 0,
  `status` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transaksi`
--

INSERT INTO `transaksi` (`id_transaksi`, `id_penyewa`, `tanggal_sewa`, `tanggal_kembali`, `tanggal_kembali_real`, `denda`, `status`) VALUES
('TRX0001', 'SW0001', '2026-04-20', '2026-04-21', NULL, 0, 'Dipinjam'),
('TRX0002', 'SW0002', '2026-04-20', '2026-04-21', '2026-04-20', 0, 'Kembali'),
('TRX0003', 'SW0003', '2026-04-20', '2026-04-21', NULL, 0, 'Dipinjam'),
('TRX0004', 'SW0004', '2026-04-21', '2026-04-22', '2026-04-21', 0, 'Kembali');

--
-- Triggers `transaksi`
--
DELIMITER $$
CREATE TRIGGER `tambah_stok` AFTER UPDATE ON `transaksi` FOR EACH ROW BEGIN
    IF NEW.status = 'Kembali' THEN
        UPDATE alat a
        JOIN detail_transaksi d 
        ON a.id_alat = d.id_alat
        SET a.stok = a.stok + d.jumlah
        WHERE d.id_transaksi = NEW.id_transaksi;
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE `user` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`id_user`, `username`, `password`) VALUES
(1, 'arsyad', 'arsila123');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_transaksi_detail`
-- (See below for the actual view)
--
CREATE TABLE `v_transaksi_detail` (
);

-- --------------------------------------------------------

--
-- Structure for view `v_transaksi_detail`
--
DROP TABLE IF EXISTS `v_transaksi_detail`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_transaksi_detail`  AS SELECT `t`.`id_transaksi` AS `id_transaksi`, `p`.`nama` AS `nama`, `a`.`nama_alat` AS `nama_alat`, `d`.`jumlah` AS `jumlah`, `d`.`hari` AS `hari`, `d`.`jumlah`* `a`.`harga` * `d`.`hari` AS `total` FROM (((`transaksi` `t` join `penyewa` `p` on(`t`.`id_penyewa` = `p`.`id_penyewa`)) join `detail_transaksi` `d` on(`t`.`id_transaksi` = `d`.`id_transaksi`)) join `alat` `a` on(`d`.`id_alat` = `a`.`id_alat`)) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `alat`
--
ALTER TABLE `alat`
  ADD PRIMARY KEY (`id_alat`);

--
-- Indexes for table `detail_transaksi`
--
ALTER TABLE `detail_transaksi`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_transaksi` (`id_transaksi`),
  ADD KEY `id_alat` (`id_alat`);

--
-- Indexes for table `penyewa`
--
ALTER TABLE `penyewa`
  ADD PRIMARY KEY (`id_penyewa`);

--
-- Indexes for table `transaksi`
--
ALTER TABLE `transaksi`
  ADD PRIMARY KEY (`id_transaksi`),
  ADD KEY `id_penyewa` (`id_penyewa`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id_user`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `detail_transaksi`
--
ALTER TABLE `detail_transaksi`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `detail_transaksi`
--
ALTER TABLE `detail_transaksi`
  ADD CONSTRAINT `detail_transaksi_ibfk_1` FOREIGN KEY (`id_transaksi`) REFERENCES `transaksi` (`id_transaksi`),
  ADD CONSTRAINT `detail_transaksi_ibfk_2` FOREIGN KEY (`id_alat`) REFERENCES `alat` (`id_alat`);

--
-- Constraints for table `transaksi`
--
ALTER TABLE `transaksi`
  ADD CONSTRAINT `transaksi_ibfk_1` FOREIGN KEY (`id_penyewa`) REFERENCES `penyewa` (`id_penyewa`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
