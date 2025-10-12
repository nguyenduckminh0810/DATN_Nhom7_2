

CREATE DATABASE AURO;
GO

USE AURO;
GO

-- ===========================================
-- 2. B?ng DANH M?C
-- ===========================================
CREATE TABLE dbo.danh_muc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten NVARCHAR(255) NOT NULL,
    slug NVARCHAR(255) UNIQUE NOT NULL,
    id_cha BIGINT NULL,
    thu_tu INT NULL,
    hoat_dong BIT NOT NULL DEFAULT 1,
    FOREIGN KEY (id_cha) REFERENCES dbo.danh_muc(id)
);

-- ===========================================
-- 3. B?ng S?N PH?M
-- ===========================================
CREATE TABLE dbo.san_pham (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten NVARCHAR(255) NOT NULL,
    mo_ta NVARCHAR(MAX) NULL,
    id_danh_muc BIGINT NOT NULL,
    gia DECIMAL(12,2) NOT NULL,
    trang_thai NVARCHAR(50) NOT NULL DEFAULT 'active',
    tao_luc DATETIME2 NOT NULL DEFAULT GETDATE(),
    cap_nhat_luc DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (id_danh_muc) REFERENCES dbo.danh_muc(id)
);

-- ===========================================
-- 4. B?ng M�U S?C
-- ===========================================
CREATE TABLE dbo.mau_sac (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten NVARCHAR(100) NOT NULL,
    ma CHAR(7) NULL
);

-- ===========================================
-- 5. B?ng K�CH C?
-- ===========================================
CREATE TABLE dbo.kich_co (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten NVARCHAR(50) NOT NULL,
    thu_tu INT NULL
);

-- ===========================================
-- 6. B?ng CH?T LI?U
-- ===========================================
CREATE TABLE dbo.chat_lieu (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten NVARCHAR(100) NOT NULL
);

-- ===========================================
-- 7. B?ng BI?N TH? S?N PH?M
-- (qu?n l� m�u/size/ch?t li?u + t?n kho)
-- ===========================================
CREATE TABLE dbo.bien_the_san_pham (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham BIGINT NOT NULL,
    sku NVARCHAR(100) UNIQUE NOT NULL,
    id_mau_sac BIGINT NOT NULL,
    id_kich_co BIGINT NOT NULL,
    id_chat_lieu BIGINT NOT NULL,
    so_luong_ton INT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id),
    FOREIGN KEY (id_mau_sac) REFERENCES dbo.mau_sac(id),
    FOREIGN KEY (id_kich_co) REFERENCES dbo.kich_co(id),
    FOREIGN KEY (id_chat_lieu) REFERENCES dbo.chat_lieu(id)
);

-- ===========================================
-- 8. B?ng H�NH ?NH S?N PH?M
-- ===========================================
CREATE TABLE dbo.hinh_anh (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_san_pham BIGINT NOT NULL,
    url NVARCHAR(500) NOT NULL,
    la_dai_dien BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_san_pham) REFERENCES dbo.san_pham(id)
);

-- ===========================================
-- 9. B?ng VOUCHER
-- ===========================================
CREATE TABLE dbo.voucher (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma NVARCHAR(50) UNIQUE NOT NULL,
    loai NVARCHAR(20) NOT NULL, -- percent / fixed
    gia_tri DECIMAL(12,2) NOT NULL,
    giam_toi_da DECIMAL(12,2) NULL,
    don_toi_thieu DECIMAL(12,2) NULL,
    ngay_bat_dau DATETIME2 NOT NULL,
    ngay_ket_thuc DATETIME2 NOT NULL,
    so_luong INT NOT NULL,
    so_luong_da_dung INT NOT NULL DEFAULT 0,
    tao_luc DATETIME2 NOT NULL DEFAULT GETDATE(),
    cap_nhat_luc DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- ===========================================
-- 10. B?ng T�I KHO?N + VAI TR�
-- ===========================================
CREATE TABLE dbo.vai_tro (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma NVARCHAR(50) UNIQUE NOT NULL,
    ten NVARCHAR(100) NOT NULL
);

CREATE TABLE dbo.tai_khoan (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(255) UNIQUE NOT NULL,
    so_dien_thoai NVARCHAR(20) UNIQUE NULL,
    mat_khau_hash NVARCHAR(255) NOT NULL,
    id_vai_tro BIGINT NOT NULL,
    trang_thai NVARCHAR(20) NOT NULL DEFAULT 'active',
    FOREIGN KEY (id_vai_tro) REFERENCES dbo.vai_tro(id)
);

-- ===========================================
-- 11. B?ng KH�CH H�NG & NH�N VI�N
-- ===========================================
CREATE TABLE dbo.khach_hang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_tai_khoan BIGINT NULL,
    ho_ten NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NULL,
    so_dien_thoai NVARCHAR(20) NULL,
    kieu NVARCHAR(20) NOT NULL DEFAULT 'thanh_vien',
    FOREIGN KEY (id_tai_khoan) REFERENCES dbo.tai_khoan(id)
);

CREATE TABLE dbo.nhan_vien (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_tai_khoan BIGINT NOT NULL,
    ho_ten NVARCHAR(255) NOT NULL,
    so_dien_thoai NVARCHAR(20) NULL,
    FOREIGN KEY (id_tai_khoan) REFERENCES dbo.tai_khoan(id)
);

-- ===========================================
-- 12. B?ng ??A CH?
-- ===========================================
CREATE TABLE dbo.dia_chi (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang BIGINT NOT NULL,
    ho_ten NVARCHAR(255) NOT NULL,
    so_dien_thoai NVARCHAR(20) NOT NULL,
    dia_chi_1 NVARCHAR(255) NOT NULL,
    tinh_thanh NVARCHAR(100) NOT NULL,
    quan_huyen NVARCHAR(100) NOT NULL,
    mac_dinh BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id)
);

-- ===========================================
-- 13. B?ng GI? H�NG & CHI TI?T
-- ===========================================
CREATE TABLE dbo.gio_hang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_khach_hang BIGINT NOT NULL,
    id_phien NVARCHAR(100) NULL,
    tien_te NVARCHAR(10) NOT NULL DEFAULT 'VND',
    FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id)
);

CREATE TABLE dbo.gio_hang_chi_tiet (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_gio_hang BIGINT NOT NULL,
    id_bien_the BIGINT NOT NULL,
    so_luong INT NOT NULL,
    gia_tai_thoi_diem DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_gio_hang) REFERENCES dbo.gio_hang(id),
    FOREIGN KEY (id_bien_the) REFERENCES dbo.bien_the_san_pham(id)
);

-- ===========================================
-- 14. B?ng ??N H�NG & CHI TI?T
-- ===========================================
CREATE TABLE dbo.don_hang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    so_don_hang NVARCHAR(50) UNIQUE NOT NULL,
    id_khach_hang BIGINT NULL,
    tam_tinh DECIMAL(12,2) NOT NULL,
    giam_gia_tong DECIMAL(12,2) NOT NULL DEFAULT 0,
    phi_van_chuyen DECIMAL(12,2) NOT NULL DEFAULT 0,
    tong_thanh_toan AS ( (tam_tinh - giam_gia_tong) + phi_van_chuyen ) PERSISTED,
    tien_te NVARCHAR(10) NOT NULL DEFAULT 'VND',
    ma_voucher NVARCHAR(50) NULL,
    dia_chi_giao_snapshot NVARCHAR(MAX) NULL,
    trang_thai NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ghi_chu NVARCHAR(500) NULL,
    dat_luc DATETIME2 NOT NULL DEFAULT GETDATE(),
    tao_luc DATETIME2 NOT NULL DEFAULT GETDATE(),
    cap_nhat_luc DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id)
);

CREATE TABLE dbo.don_hang_chi_tiet (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_don_hang BIGINT NOT NULL,
    id_bien_the BIGINT NOT NULL,
    so_luong INT NOT NULL,
    gia_tai_thoi_diem DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_don_hang) REFERENCES dbo.don_hang(id),
    FOREIGN KEY (id_bien_the) REFERENCES dbo.bien_the_san_pham(id)
);
-- ===========================================
-- B?ng VOUCHER_KHACH
-- (voucher g?n cho t?ng kh�ch h�ng)
-- ===========================================
CREATE TABLE dbo.voucher_khach (
    id_voucher BIGINT NOT NULL,
    id_khach_hang BIGINT NOT NULL,
    trang_thai NVARCHAR(20) NOT NULL DEFAULT 'chua_dung', 
    -- chua_dung / da_dung / het_han
    ngay_cap DATETIME2 NOT NULL DEFAULT GETDATE(),
    PRIMARY KEY (id_voucher, id_khach_hang),
    FOREIGN KEY (id_voucher) REFERENCES dbo.voucher(id),
    FOREIGN KEY (id_khach_hang) REFERENCES dbo.khach_hang(id)
);
    INSERT INTO dbo.vai_tro(ma, ten)
    VALUES (N'CUS', N'Khách hàng'),
           (N'GST', N'Khách vãng lai'),
           (N'STF', N'Nhân viên'),
           (N'ADM', N'Quản trị viên');