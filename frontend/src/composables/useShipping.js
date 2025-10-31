import { ref, reactive, computed } from 'vue'
import shippingService from '@/services/shippingService'

// 🎯 MOCK DATA - Dữ liệu mẫu với FULL 63 tỉnh thành Việt Nam
const MOCK_DATA = {
  provinces: [
    { ProvinceID: 269, ProvinceName: 'Lào Cai', Code: '10' },
    { ProvinceID: 271, ProvinceName: 'Điện Biên', Code: '11' },
    { ProvinceID: 273, ProvinceName: 'Lai Châu', Code: '12' },
    { ProvinceID: 275, ProvinceName: 'Sơn La', Code: '14' },
    { ProvinceID: 277, ProvinceName: 'Yên Bái', Code: '15' },
    { ProvinceID: 279, ProvinceName: 'Hoà Bình', Code: '17' },
    { ProvinceID: 281, ProvinceName: 'Thái Nguyên', Code: '19' },
    { ProvinceID: 283, ProvinceName: 'Lạng Sơn', Code: '20' },
    { ProvinceID: 285, ProvinceName: 'Quảng Ninh', Code: '22' },
    { ProvinceID: 287, ProvinceName: 'Bắc Giang', Code: '24' },
    { ProvinceID: 289, ProvinceName: 'Phú Thọ', Code: '25' },
    { ProvinceID: 291, ProvinceName: 'Vĩnh Phúc', Code: '26' },
    { ProvinceID: 293, ProvinceName: 'Bắc Ninh', Code: '27' },
    { ProvinceID: 295, ProvinceName: 'Hải Dương', Code: '30' },
    { ProvinceID: 201, ProvinceName: 'Hà Nội', Code: '01' },
    { ProvinceID: 297, ProvinceName: 'Hải Phòng', Code: '31' },
    { ProvinceID: 299, ProvinceName: 'Hưng Yên', Code: '33' },
    { ProvinceID: 301, ProvinceName: 'Thái Bình', Code: '34' },
    { ProvinceID: 303, ProvinceName: 'Hà Nam', Code: '35' },
    { ProvinceID: 305, ProvinceName: 'Nam Định', Code: '36' },
    { ProvinceID: 307, ProvinceName: 'Ninh Bình', Code: '37' },
    { ProvinceID: 309, ProvinceName: 'Thanh Hóa', Code: '38' },
    { ProvinceID: 311, ProvinceName: 'Nghệ An', Code: '40' },
    { ProvinceID: 313, ProvinceName: 'Hà Tĩnh', Code: '42' },
    { ProvinceID: 315, ProvinceName: 'Quảng Bình', Code: '44' },
    { ProvinceID: 317, ProvinceName: 'Quảng Trị', Code: '45' },
    { ProvinceID: 319, ProvinceName: 'Thừa Thiên Huế', Code: '46' },
    { ProvinceID: 203, ProvinceName: 'Đà Nẵng', Code: '48' },
    { ProvinceID: 321, ProvinceName: 'Quảng Nam', Code: '49' },
    { ProvinceID: 323, ProvinceName: 'Quảng Ngãi', Code: '51' },
    { ProvinceID: 325, ProvinceName: 'Bình Định', Code: '52' },
    { ProvinceID: 327, ProvinceName: 'Phú Yên', Code: '54' },
    { ProvinceID: 329, ProvinceName: 'Khánh Hòa', Code: '56' },
    { ProvinceID: 331, ProvinceName: 'Ninh Thuận', Code: '58' },
    { ProvinceID: 333, ProvinceName: 'Bình Thuận', Code: '60' },
    { ProvinceID: 335, ProvinceName: 'Kon Tum', Code: '62' },
    { ProvinceID: 337, ProvinceName: 'Gia Lai', Code: '64' },
    { ProvinceID: 339, ProvinceName: 'Đắk Lắk', Code: '66' },
    { ProvinceID: 341, ProvinceName: 'Đắk Nông', Code: '67' },
    { ProvinceID: 343, ProvinceName: 'Lâm Đồng', Code: '68' },
    { ProvinceID: 345, ProvinceName: 'Bình Phước', Code: '70' },
    { ProvinceID: 347, ProvinceName: 'Tây Ninh', Code: '72' },
    { ProvinceID: 349, ProvinceName: 'Bình Dương', Code: '74' },
    { ProvinceID: 351, ProvinceName: 'Đồng Nai', Code: '75' },
    { ProvinceID: 353, ProvinceName: 'Bà Rịa - Vũng Tàu', Code: '77' },
    { ProvinceID: 202, ProvinceName: 'TP. Hồ Chí Minh', Code: '79' },
    { ProvinceID: 355, ProvinceName: 'Long An', Code: '80' },
    { ProvinceID: 357, ProvinceName: 'Tiền Giang', Code: '82' },
    { ProvinceID: 359, ProvinceName: 'Bến Tre', Code: '83' },
    { ProvinceID: 361, ProvinceName: 'Trà Vinh', Code: '84' },
    { ProvinceID: 363, ProvinceName: 'Vĩnh Long', Code: '86' },
    { ProvinceID: 365, ProvinceName: 'Đồng Tháp', Code: '87' },
    { ProvinceID: 367, ProvinceName: 'An Giang', Code: '89' },
    { ProvinceID: 369, ProvinceName: 'Kiên Giang', Code: '91' },
    { ProvinceID: 204, ProvinceName: 'Cần Thơ', Code: '92' },
    { ProvinceID: 371, ProvinceName: 'Hậu Giang', Code: '93' },
    { ProvinceID: 373, ProvinceName: 'Sóc Trăng', Code: '94' },
    { ProvinceID: 375, ProvinceName: 'Bạc Liêu', Code: '95' },
    { ProvinceID: 377, ProvinceName: 'Cà Mau', Code: '96' },
    { ProvinceID: 380, ProvinceName: 'Hà Giang', Code: '02' },
    { ProvinceID: 382, ProvinceName: 'Cao Bằng', Code: '04' },
    { ProvinceID: 384, ProvinceName: 'Bắc Kạn', Code: '06' },
    { ProvinceID: 386, ProvinceName: 'Tuyên Quang', Code: '08' },
  ],
  districts: {
    202: [ // TP.HCM
      { DistrictID: 1542, DistrictName: 'Quận 1', Code: '1542' },
      { DistrictID: 1443, DistrictName: 'Quận 3', Code: '1443' },
      { DistrictID: 1462, DistrictName: 'Quận 10', Code: '1462' },
      { DistrictID: 3695, DistrictName: 'Thủ Đức', Code: '3695' },
      { DistrictID: 1444, DistrictName: 'Quận 4', Code: '1444' },
      { DistrictID: 1446, DistrictName: 'Quận 5', Code: '1446' },
      { DistrictID: 1447, DistrictName: 'Quận 6', Code: '1447' },
      { DistrictID: 1449, DistrictName: 'Quận 7', Code: '1449' },
      { DistrictID: 1450, DistrictName: 'Quận 8', Code: '1450' },
      { DistrictID: 1452, DistrictName: 'Quận 11', Code: '1452' },
      { DistrictID: 1453, DistrictName: 'Quận 12', Code: '1453' },
      { DistrictID: 1454, DistrictName: 'Quận Bình Tân', Code: '1454' },
      { DistrictID: 1455, DistrictName: 'Quận Bình Thạnh', Code: '1455' },
      { DistrictID: 1456, DistrictName: 'Quận Gò Vấp', Code: '1456' },
      { DistrictID: 1457, DistrictName: 'Quận Phú Nhuận', Code: '1457' },
      { DistrictID: 1458, DistrictName: 'Quận Tân Bình', Code: '1458' },
      { DistrictID: 1459, DistrictName: 'Quận Tân Phú', Code: '1459' },
    ],
    201: [ // Hà Nội
      { DistrictID: 1482, DistrictName: 'Ba Đình', Code: '1482' },
      { DistrictID: 1451, DistrictName: 'Hoàn Kiếm', Code: '1451' },
      { DistrictID: 1452, DistrictName: 'Đống Đa', Code: '1452' },
      { DistrictID: 1488, DistrictName: 'Hai Bà Trưng', Code: '1488' },
      { DistrictID: 1490, DistrictName: 'Hoàng Mai', Code: '1490' },
      { DistrictID: 1491, DistrictName: 'Thanh Xuân', Code: '1491' },
      { DistrictID: 1533, DistrictName: 'Long Biên', Code: '1533' },
      { DistrictID: 1534, DistrictName: 'Nam Từ Liêm', Code: '1534' },
      { DistrictID: 1535, DistrictName: 'Bắc Từ Liêm', Code: '1535' },
      { DistrictID: 1542, DistrictName: 'Cầu Giấy', Code: '1542' },
    ],
    203: [ // Đà Nẵng
      { DistrictID: 1580, DistrictName: 'Hải Châu', Code: '1580' },
      { DistrictID: 3440, DistrictName: 'Thanh Khê', Code: '3440' },
      { DistrictID: 1581, DistrictName: 'Sơn Trà', Code: '1581' },
      { DistrictID: 1582, DistrictName: 'Ngũ Hành Sơn', Code: '1582' },
      { DistrictID: 1583, DistrictName: 'Liên Chiểu', Code: '1583' },
      { DistrictID: 1584, DistrictName: 'Cẩm Lệ', Code: '1584' },
    ],
    204: [ // Cần Thơ
      { DistrictID: 1711, DistrictName: 'Ninh Kiều', Code: '1711' },
      { DistrictID: 1712, DistrictName: 'Ô Môn', Code: '1712' },
      { DistrictID: 1713, DistrictName: 'Bình Thủy', Code: '1713' },
      { DistrictID: 1714, DistrictName: 'Cái Răng', Code: '1714' },
      { DistrictID: 1715, DistrictName: 'Thốt Nốt', Code: '1715' },
    ],
    297: [ // Hải Phòng - FULL 15 quận/huyện
      { DistrictID: 1643, DistrictName: 'Quận Hồng Bàng', Code: '1643' },
      { DistrictID: 1644, DistrictName: 'Quận Ngô Quyền', Code: '1644' },
      { DistrictID: 1645, DistrictName: 'Quận Lê Chân', Code: '1645' },
      { DistrictID: 1646, DistrictName: 'Quận Hải An', Code: '1646' },
      { DistrictID: 1647, DistrictName: 'Quận Kiến An', Code: '1647' },
      { DistrictID: 1648, DistrictName: 'Quận Đồ Sơn', Code: '1648' },
      { DistrictID: 1649, DistrictName: 'Quận Dương Kinh', Code: '1649' },
      { DistrictID: 1650, DistrictName: 'Huyện An Dương', Code: '1650' },
      { DistrictID: 1651, DistrictName: 'Huyện An Lão', Code: '1651' },
      { DistrictID: 1652, DistrictName: 'Huyện Kiến Thụy', Code: '1652' },
      { DistrictID: 1653, DistrictName: 'Huyện Tiên Lãng', Code: '1653' },
      { DistrictID: 1654, DistrictName: 'Huyện Vĩnh Bảo', Code: '1654' },
      { DistrictID: 1655, DistrictName: 'Huyện Cát Hải', Code: '1655' },
      { DistrictID: 1656, DistrictName: 'Huyện Bạch Long Vĩ', Code: '1656' },
      { DistrictID: 1657, DistrictName: 'Huyện Thủy Nguyên', Code: '1657' },
    ],
    349: [ // Bình Dương
      { DistrictID: 1573, DistrictName: 'Thủ Dầu Một', Code: '1573' },
      { DistrictID: 1574, DistrictName: 'Dĩ An', Code: '1574' },
      { DistrictID: 1575, DistrictName: 'Thuận An', Code: '1575' },
      { DistrictID: 1576, DistrictName: 'Bến Cát', Code: '1576' },
      { DistrictID: 1577, DistrictName: 'Tân Uyên', Code: '1577' },
    ],
    351: [ // Đồng Nai
      { DistrictID: 1563, DistrictName: 'Biên Hòa', Code: '1563' },
      { DistrictID: 1564, DistrictName: 'Long Khánh', Code: '1564' },
      { DistrictID: 1565, DistrictName: 'Nhơn Trạch', Code: '1565' },
      { DistrictID: 1566, DistrictName: 'Trảng Bom', Code: '1566' },
    ],
    353: [ // Bà Rịa - Vũng Tàu
      { DistrictID: 1555, DistrictName: 'Vũng Tàu', Code: '1555' },
      { DistrictID: 1556, DistrictName: 'Bà Rịa', Code: '1556' },
      { DistrictID: 1557, DistrictName: 'Châu Đức', Code: '1557' },
      { DistrictID: 1558, DistrictName: 'Xuyên Mộc', Code: '1558' },
    ],
    285: [ // Quảng Ninh
      { DistrictID: 1617, DistrictName: 'Hạ Long', Code: '1617' },
      { DistrictID: 1618, DistrictName: 'Móng Cái', Code: '1618' },
      { DistrictID: 1619, DistrictName: 'Cẩm Phả', Code: '1619' },
      { DistrictID: 1620, DistrictName: 'Uông Bí', Code: '1620' },
      { DistrictID: 1621, DistrictName: 'Quảng Yên', Code: '1621' },
    ],
    329: [ // Khánh Hòa
      { DistrictID: 1569, DistrictName: 'Nha Trang', Code: '1569' },
      { DistrictID: 1570, DistrictName: 'Cam Ranh', Code: '1570' },
      { DistrictID: 1571, DistrictName: 'Ninh Hòa', Code: '1571' },
      { DistrictID: 1572, DistrictName: 'Vạn Ninh', Code: '1572' },
    ],
  },
  wards: {
    1542: [ // Quận 1 - HCM
      { WardCode: '21211', WardName: 'Phường Bến Nghé' },
      { WardCode: '21212', WardName: 'Phường Bến Thành' },
      { WardCode: '21213', WardName: 'Phường Nguyễn Thái Bình' },
      { WardCode: '21214', WardName: 'Phường Phạm Ngũ Lão' },
      { WardCode: '21215', WardName: 'Phường Cô Giang' },
    ],
    1443: [ // Quận 3 - HCM
      { WardCode: '20308', WardName: 'Phường 01' },
      { WardCode: '20309', WardName: 'Phường 02' },
      { WardCode: '20310', WardName: 'Phường 03' },
      { WardCode: '20311', WardName: 'Phường 04' },
    ],
    3695: [ // Thủ Đức - HCM
      { WardCode: '90737', WardName: 'Phường Linh Xuân' },
      { WardCode: '90738', WardName: 'Phường Bình Chiểu' },
      { WardCode: '90739', WardName: 'Phường Linh Trung' },
      { WardCode: '90740', WardName: 'Phường Tam Bình' },
    ],
    1482: [ // Ba Đình - Hà Nội
      { WardCode: '11007', WardName: 'Phường Phúc Xá' },
      { WardCode: '11008', WardName: 'Phường Trúc Bạch' },
      { WardCode: '11009', WardName: 'Phường Vĩnh Phúc' },
      { WardCode: '11010', WardName: 'Phường Cống Vị' },
    ],
    1451: [ // Hoàn Kiếm - Hà Nội
      { WardCode: '10101', WardName: 'Phường Hàng Bạc' },
      { WardCode: '10102', WardName: 'Phường Hàng Gai' },
      { WardCode: '10103', WardName: 'Phường Hàng Trống' },
      { WardCode: '10104', WardName: 'Phường Hàng Bông' },
    ],
    1580: [ // Hải Châu - Đà Nẵng
      { WardCode: '550501', WardName: 'Phường Thạch Thang' },
      { WardCode: '550502', WardName: 'Phường Hải Châu 1' },
      { WardCode: '550503', WardName: 'Phường Hải Châu 2' },
      { WardCode: '550504', WardName: 'Phường Phước Ninh' },
    ],
    1711: [ // Ninh Kiều - Cần Thơ
      { WardCode: '92101', WardName: 'Phường Cái Khế' },
      { WardCode: '92102', WardName: 'Phường An Hòa' },
      { WardCode: '92103', WardName: 'Phường Thới Bình' },
      { WardCode: '92104', WardName: 'Phường An Nghiệp' },
    ],
    1643: [ // Hồng Bàng - Hải Phòng
      { WardCode: '31101', WardName: 'Phường Quán Toan' },
      { WardCode: '31102', WardName: 'Phường Hùng Vương' },
      { WardCode: '31103', WardName: 'Phường Sở Dầu' },
      { WardCode: '31104', WardName: 'Phường Thượng Lý' },
    ],
    1644: [ // Ngô Quyền - Hải Phòng
      { WardCode: '31201', WardName: 'Phường Máy Chai' },
      { WardCode: '31202', WardName: 'Phường Máy Tơ' },
      { WardCode: '31203', WardName: 'Phường Vạn Mỹ' },
      { WardCode: '31204', WardName: 'Phường Cầu Tre' },
    ],
    1645: [ // Lê Chân - Hải Phòng
      { WardCode: '31301', WardName: 'Phường Cát Dài' },
      { WardCode: '31302', WardName: 'Phường An Biên' },
      { WardCode: '31303', WardName: 'Phường Lam Sơn' },
      { WardCode: '31304', WardName: 'Phường An Dương' },
    ],
    1646: [ // Hải An - Hải Phòng
      { WardCode: '31401', WardName: 'Phường Đông Hải 1' },
      { WardCode: '31402', WardName: 'Phường Đông Hải 2' },
      { WardCode: '31403', WardName: 'Phường Đằng Lâm' },
      { WardCode: '31404', WardName: 'Phường Nam Hải' },
    ],
    1650: [ // Huyện An Dương - Hải Phòng
      { WardCode: '31801', WardName: 'Thị trấn An Dương' },
      { WardCode: '31802', WardName: 'Xã Lê Thiện' },
      { WardCode: '31803', WardName: 'Xã Đại Bản' },
      { WardCode: '31804', WardName: 'Xã An Hoà' },
      { WardCode: '31805', WardName: 'Xã Hồng Phong' },
      { WardCode: '31806', WardName: 'Xã Đặng Cương' },
      { WardCode: '31807', WardName: 'Xã Đồng Thái' },
      { WardCode: '31808', WardName: 'Xã Quốc Tuấn' },
      { WardCode: '31809', WardName: 'Xã An Đồng' },
      { WardCode: '31810', WardName: 'Xã Hồng Thái' },
    ],
    1651: [ // Huyện An Lão - Hải Phòng
      { WardCode: '31901', WardName: 'Thị trấn An Lão' },
      { WardCode: '31902', WardName: 'Xã Bát Trang' },
      { WardCode: '31903', WardName: 'Xã Trường Thọ' },
      { WardCode: '31904', WardName: 'Xã Trường Thành' },
    ],
    1652: [ // Huyện Kiến Thụy - Hải Phòng
      { WardCode: '32001', WardName: 'Thị trấn Núi Đèo' },
      { WardCode: '32002', WardName: 'Xã Đông Phương' },
      { WardCode: '32003', WardName: 'Xã Thuận Thiên' },
      { WardCode: '32004', WardName: 'Xã Kiến Quốc' },
    ],
    1653: [ // Huyện Tiên Lãng - Hải Phòng
      { WardCode: '32101', WardName: 'Thị trấn Tiên Lãng' },
      { WardCode: '32102', WardName: 'Xã Đại Thắng' },
      { WardCode: '32103', WardName: 'Xã Tiên Cường' },
      { WardCode: '32104', WardName: 'Xã Tự Cường' },
    ],
    1654: [ // Huyện Vĩnh Bảo - Hải Phòng
      { WardCode: '32201', WardName: 'Thị trấn Vĩnh Bảo' },
      { WardCode: '32202', WardName: 'Xã Dũng Tiến' },
      { WardCode: '32203', WardName: 'Xã Giang Biên' },
      { WardCode: '32204', WardName: 'Xã Trung Lập' },
    ],
    1657: [ // Huyện Thủy Nguyên - Hải Phòng
      { WardCode: '32501', WardName: 'Thị trấn Núi Đối' },
      { WardCode: '32502', WardName: 'Xã Ngũ Phúc' },
      { WardCode: '32503', WardName: 'Xã Kênh Giang' },
      { WardCode: '32504', WardName: 'Xã Liên Khê' },
    ],
    1573: [ // Thủ Dầu Một - Bình Dương
      { WardCode: '74101', WardName: 'Phường Phú Hòa' },
      { WardCode: '74102', WardName: 'Phường Phú Thọ' },
      { WardCode: '74103', WardName: 'Phường Phú Lợi' },
      { WardCode: '74104', WardName: 'Phường Phú Cường' },
    ],
    1563: [ // Biên Hòa - Đồng Nai
      { WardCode: '75101', WardName: 'Phường Trảng Dài' },
      { WardCode: '75102', WardName: 'Phường Tân Phong' },
      { WardCode: '75103', WardName: 'Phường Tân Biên' },
      { WardCode: '75104', WardName: 'Phường Hố Nai' },
    ],
    1555: [ // Vũng Tàu - BRVT
      { WardCode: '77101', WardName: 'Phường 1' },
      { WardCode: '77102', WardName: 'Phường 2' },
      { WardCode: '77103', WardName: 'Phường 3' },
      { WardCode: '77104', WardName: 'Phường Thắng Tam' },
    ],
    1617: [ // Hạ Long - Quảng Ninh
      { WardCode: '22101', WardName: 'Phường Bãi Cháy' },
      { WardCode: '22102', WardName: 'Phường Hồng Gai' },
      { WardCode: '22103', WardName: 'Phường Hà Khánh' },
      { WardCode: '22104', WardName: 'Phường Hà Phong' },
    ],
    1569: [ // Nha Trang - Khánh Hòa
      { WardCode: '56101', WardName: 'Phường Vĩnh Hòa' },
      { WardCode: '56102', WardName: 'Phường Vĩnh Phước' },
      { WardCode: '56103', WardName: 'Phường Phước Long' },
      { WardCode: '56104', WardName: 'Phường Phước Tiến' },
    ],
  }
}

// Flag để bật/tắt mock mode
const USE_MOCK_DATA = true // ⚠️ BẬT LẠI - Backend chưa sẵn sàng

/**
 * Composable để xử lý shipping GHN trong Vue components
 * @returns {Object} Shipping state và methods
 */
export function useShipping() {
  // State
  const provinces = ref([])
  const districts = ref([])
  const wards = ref([])
  const services = ref([])

  const selectedProvince = ref(null)
  const selectedDistrict = ref(null)
  const selectedWard = ref(null)
  const selectedService = ref(null)

  const shippingFee = ref(0)
  const expectedDeliveryTime = ref(null)

  const loading = reactive({
    provinces: false,
    districts: false,
    wards: false,
    services: false,
    calculating: false,
  })

  const errors = reactive({
    provinces: null,
    districts: null,
    wards: null,
    services: null,
    calculating: null,
  })

  // Computed
  const hasSelectedAddress = computed(() => {
    return selectedProvince.value && selectedDistrict.value && selectedWard.value
  })

  const canCalculateFee = computed(() => {
    return hasSelectedAddress.value && selectedService.value
  })

  const formattedShippingFee = computed(() => {
    return shippingService.formatCurrency(shippingFee.value)
  })

  // Methods
  /**
   * Load danh sách tỉnh/thành phố
   */
  const loadProvinces = async () => {
    loading.provinces = true
    errors.provinces = null

    try {
      // 🎯 MOCK MODE: Sử dụng dữ liệu mẫu
      if (USE_MOCK_DATA) {
        await new Promise(resolve => setTimeout(resolve, 500)) // Giả lập network delay
        provinces.value = MOCK_DATA.provinces
        console.log('✅ [MOCK] Loaded provinces:', provinces.value.length)
        return
      }

      // Real API call
      provinces.value = await shippingService.getProvinces()
    } catch (error) {
      errors.provinces = error.message
      console.error('Error loading provinces:', error)
    } finally {
      loading.provinces = false
    }
  }

  /**
   * Load danh sách quận/huyện
   * @param {number} provinceId - ID của tỉnh/thành phố
   */
  const loadDistricts = async (provinceId) => {
    if (!provinceId) {
      districts.value = []
      return
    }

    loading.districts = true
    errors.districts = null

    // Reset dependent selections
    selectedDistrict.value = null
    selectedWard.value = null
    wards.value = []

    try {
      // 🎯 MOCK MODE: Sử dụng dữ liệu mẫu
      if (USE_MOCK_DATA) {
        await new Promise(resolve => setTimeout(resolve, 400))
        districts.value = MOCK_DATA.districts[provinceId] || []
        console.log('✅ [MOCK] Loaded districts for province', provinceId, ':', districts.value.length)
        return
      }

      // Real API call
      districts.value = await shippingService.getDistricts(provinceId)
    } catch (error) {
      errors.districts = error.message
      console.error('Error loading districts:', error)
    } finally {
      loading.districts = false
    }
  }

  /**
   * Load danh sách phường/xã
   * @param {number} districtId - ID của quận/huyện
   */
  const loadWards = async (districtId) => {
    if (!districtId) {
      wards.value = []
      return
    }

    loading.wards = true
    errors.wards = null

    // Reset dependent selections
    selectedWard.value = null

    try {
      // 🎯 MOCK MODE: Sử dụng dữ liệu mẫu
      if (USE_MOCK_DATA) {
        await new Promise(resolve => setTimeout(resolve, 300))
        wards.value = MOCK_DATA.wards[districtId] || []
        console.log('✅ [MOCK] Loaded wards for district', districtId, ':', wards.value.length)
        return
      }

      // Real API call
      wards.value = await shippingService.getWards(districtId)
    } catch (error) {
      errors.wards = error.message
      console.error('Error loading wards:', error)
    } finally {
      loading.wards = false
    }
  }

  /**
   * Load danh sách dịch vụ vận chuyển
   * @param {number} toDistrictId - ID quận/huyện đích
   */
  const loadServices = async (toDistrictId) => {
    if (!toDistrictId) {
      services.value = []
      return
    }

    loading.services = true
    errors.services = null

    try {
      services.value = await shippingService.getServices(toDistrictId)

      // Auto select default service if available
      if (services.value.length > 0 && !selectedService.value) {
        const expressService = services.value.find(
          (s) => s.service_id === shippingService.SERVICE_IDS.EXPRESS,
        )
        selectedService.value = expressService
          ? expressService.service_id
          : services.value[0].service_id
      }
    } catch (error) {
      errors.services = error.message
      console.error('Error loading services:', error)
    } finally {
      loading.services = false
    }
  }

  /**
   * Tính phí vận chuyển
   * @param {Object} params - Thông tin tính phí
   * @param {number} params.totalWeight - Tổng khối lượng (gram)
   * @param {number} params.insuranceValue - Giá trị đơn hàng (VNĐ)
   * @param {number} [params.serviceId] - Mã dịch vụ (optional, sẽ dùng selectedService nếu không có)
   */
  const calculateShippingFee = async (params) => {
    const { totalWeight, insuranceValue, serviceId } = params

    if (!selectedDistrict.value || !selectedWard.value) {
      errors.calculating = 'Vui lòng chọn đầy đủ địa chỉ giao hàng'
      return null
    }

    const effectiveServiceId = serviceId || selectedService.value || 53320

    loading.calculating = true
    errors.calculating = null

    try {
      // 🎯 MOCK MODE: Tính phí ship giả lập với công thức linh động
      if (USE_MOCK_DATA) {
        await new Promise(resolve => setTimeout(resolve, 600))
        
        // 💰 CÔNG THỨC PHÍ SHIP LINH ĐỘNG
        let baseFee = 20000 // Base fee mặc định
        
        // 1. Phí theo tỉnh (distance fee)
        let provinceFee = 0
        if (selectedProvince.value === 202) {
          // TP.HCM → trong thành phố
          provinceFee = 0
        } else if (selectedProvince.value === 201) {
          // Hà Nội → xa
          provinceFee = 30000
        } else if (selectedProvince.value === 203) {
          // Đà Nẵng → rất xa
          provinceFee = 40000
        } else if (selectedProvince.value === 204) {
          // Cần Thơ → xa vừa
          provinceFee = 25000
        }
        
        // 2. Phí theo quận (zone fee)
        let districtFee = 0
        if (selectedDistrict.value === 1542) {
          // Quận 1 - nội thành
          districtFee = 0
        } else if (selectedDistrict.value === 3695) {
          // Thủ Đức - xa hơn
          districtFee = 10000
        } else if (selectedDistrict.value === 1443 || selectedDistrict.value === 1462) {
          // Quận 3, 10 - trung tâm
          districtFee = 5000
        } else {
          // Quận khác
          districtFee = 8000
        }
        
        // 3. Phí theo trọng lượng
        const weightFee = Math.ceil(totalWeight / 1000) * 5000 // 5k mỗi kg
        
        // 4. Phí bảo hiểm
        const insuranceFee = insuranceValue > 3000000 ? Math.ceil(insuranceValue * 0.005) : 0
        
        // 🎯 TỔNG PHÍ = Base + Province + District + Weight + Insurance
        const totalFee = baseFee + provinceFee + districtFee + weightFee + insuranceFee
        
        shippingFee.value = totalFee
        
        // Thời gian giao hàng phụ thuộc vào khoảng cách
        let daysToDeliver = 2 // Default
        if (selectedProvince.value === 202) {
          daysToDeliver = 1 // TP.HCM: 1 ngày
        } else if (selectedProvince.value === 201 || selectedProvince.value === 203) {
          daysToDeliver = 3 // Hà Nội, Đà Nẵng: 3 ngày
        } else {
          daysToDeliver = 2 // Khác: 2 ngày
        }
        
        expectedDeliveryTime.value = new Date(Date.now() + daysToDeliver * 24 * 60 * 60 * 1000).toISOString()
        
        console.log('✅ [MOCK] Calculated shipping fee:', {
          province: selectedProvince.value,
          district: selectedDistrict.value,
          totalWeight,
          insuranceValue,
          breakdown: {
            baseFee,
            provinceFee,
            districtFee,
            weightFee,
            insuranceFee
          },
          totalFee,
          daysToDeliver
        })
        
        return {
          success: true,
          shippingFee: totalFee,
          expectedDeliveryTime: expectedDeliveryTime.value,
          message: 'Tính phí vận chuyển thành công (mock)'
        }
      }

      // Real API call
      const response = await shippingService.calculateShippingFee({
        toDistrictId: selectedDistrict.value,
        toWardCode: selectedWard.value,
        totalWeight,
        insuranceValue,
        serviceId: effectiveServiceId,
      })

      if (response.success) {
        shippingFee.value = response.shippingFee || 0
        expectedDeliveryTime.value = response.expectedDeliveryTime || null
        return response
      } else {
        throw new Error(response.message || 'Không thể tính phí vận chuyển')
      }
    } catch (error) {
      errors.calculating = error.message
      console.error('Error calculating shipping fee:', error)
      throw error
    } finally {
      loading.calculating = false
    }
  }

  /**
   * Tính phí vận chuyển chi tiết (có kích thước)
   * @param {Object} params - Thông tin chi tiết
   */
  const calculateShippingFeeFull = async (params) => {
    if (!selectedDistrict.value || !selectedWard.value) {
      errors.calculating = 'Vui lòng chọn đầy đủ địa chỉ giao hàng'
      return null
    }

    loading.calculating = true
    errors.calculating = null

    try {
      const response = await shippingService.calculateShippingFeeFull({
        ...params,
        toDistrictId: selectedDistrict.value,
        toWardCode: selectedWard.value,
        serviceId: params.serviceId || selectedService.value,
      })

      if (response.success) {
        shippingFee.value = response.shippingFee || 0
        expectedDeliveryTime.value = response.expectedDeliveryTime || null
        return response
      } else {
        throw new Error(response.message || 'Không thể tính phí vận chuyển')
      }
    } catch (error) {
      errors.calculating = error.message
      console.error('Error calculating full shipping fee:', error)
      throw error
    } finally {
      loading.calculating = false
    }
  }

  /**
   * Reset tất cả state
   */
  const reset = () => {
    provinces.value = []
    districts.value = []
    wards.value = []
    services.value = []
    selectedProvince.value = null
    selectedDistrict.value = null
    selectedWard.value = null
    selectedService.value = null
    shippingFee.value = 0
    expectedDeliveryTime.value = null

    Object.keys(loading).forEach((key) => {
      loading[key] = false
    })
    Object.keys(errors).forEach((key) => {
      errors[key] = null
    })
  }

  /**
   * Reset phí vận chuyển
   */
  const resetFee = () => {
    shippingFee.value = 0
    expectedDeliveryTime.value = null
    errors.calculating = null
  }

  /**
   * Set địa chỉ từ object
   * @param {Object} address - Địa chỉ
   */
  const setAddress = async (address) => {
    if (address.provinceId) {
      selectedProvince.value = address.provinceId
      await loadDistricts(address.provinceId)
    }

    if (address.districtId) {
      selectedDistrict.value = address.districtId
      await loadWards(address.districtId)
      await loadServices(address.districtId)
    }

    if (address.wardCode) {
      selectedWard.value = address.wardCode
    }
  }

  /**
   * Get địa chỉ hiện tại
   * @returns {Object} Địa chỉ
   */
  const getAddress = () => {
    return {
      provinceId: selectedProvince.value,
      districtId: selectedDistrict.value,
      wardCode: selectedWard.value,
      provinceName: provinces.value.find((p) => p.ProvinceID === selectedProvince.value)
        ?.ProvinceName,
      districtName: districts.value.find((d) => d.DistrictID === selectedDistrict.value)
        ?.DistrictName,
      wardName: wards.value.find((w) => w.WardCode === selectedWard.value)?.WardName,
    }
  }

  /**
   * Validate địa chỉ
   * @param {Object} address - Địa chỉ cần validate
   * @returns {Object} { valid: boolean, errors: Array }
   */
  const validateAddress = (address) => {
    return shippingService.validateAddress(address)
  }

  return {
    // State
    provinces,
    districts,
    wards,
    services,
    selectedProvince,
    selectedDistrict,
    selectedWard,
    selectedService,
    shippingFee,
    expectedDeliveryTime,
    loading,
    errors,

    // Computed
    hasSelectedAddress,
    canCalculateFee,
    formattedShippingFee,

    // Methods
    loadProvinces,
    loadDistricts,
    loadWards,
    loadServices,
    calculateShippingFee,
    calculateShippingFeeFull,
    reset,
    resetFee,
    setAddress,
    getAddress,
    validateAddress,

    // Service methods (pass-through)
    formatCurrency: shippingService.formatCurrency,
    formatWeight: shippingService.formatWeight,
    getServiceName: shippingService.getServiceName,
    SERVICE_IDS: shippingService.SERVICE_IDS,
  }
}
