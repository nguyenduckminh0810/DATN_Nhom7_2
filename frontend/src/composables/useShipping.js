import { ref, reactive, computed, watch } from 'vue'
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
    // Phú Thọ (289) - MỚI THÊM
    289: [
      { DistrictID: 2891, DistrictName: 'TP Việt Trì', Code: '2891' },
      { DistrictID: 2892, DistrictName: 'TX Phú Thọ', Code: '2892' },
      { DistrictID: 2893, DistrictName: 'Đoan Hùng', Code: '2893' },
      { DistrictID: 2894, DistrictName: 'Hạ Hoà', Code: '2894' },
      { DistrictID: 2895, DistrictName: 'Thanh Ba', Code: '2895' },
      { DistrictID: 2896, DistrictName: 'Phù Ninh', Code: '2896' },
      { DistrictID: 2897, DistrictName: 'Yên Lập', Code: '2897' },
      { DistrictID: 2898, DistrictName: 'Cẩm Khê', Code: '2898' },
      { DistrictID: 2899, DistrictName: 'Tam Nông', Code: '2899' },
      { DistrictID: 2890, DistrictName: 'Lâm Thao', Code: '2890' },
    ],
    // Hưng Yên (299) - MỚI THÊM
    299: [
      { DistrictID: 2991, DistrictName: 'TP Hưng Yên', Code: '2991' },
      { DistrictID: 2992, DistrictName: 'Văn Lâm', Code: '2992' },
      { DistrictID: 2993, DistrictName: 'Văn Giang', Code: '2993' },
      { DistrictID: 2994, DistrictName: 'Yên Mỹ', Code: '2994' },
      { DistrictID: 2995, DistrictName: 'Mỹ Hào', Code: '2995' },
      { DistrictID: 2996, DistrictName: 'Ân Thi', Code: '2996' },
      { DistrictID: 2997, DistrictName: 'Khoái Châu', Code: '2997' },
      { DistrictID: 2998, DistrictName: 'Kim Động', Code: '2998' },
      { DistrictID: 2999, DistrictName: 'Tiên Lữ', Code: '2999' },
      { DistrictID: 2990, DistrictName: 'Phù Cừ', Code: '2990' },
    ],
    // Hà Nam (303)
    303: [
      { DistrictID: 3031, DistrictName: 'TP Phủ Lý', Code: '3031' },
      { DistrictID: 3032, DistrictName: 'Duy Tiên', Code: '3032' },
      { DistrictID: 3033, DistrictName: 'Kim Bảng', Code: '3033' },
      { DistrictID: 3034, DistrictName: 'Lý Nhân', Code: '3034' },
      { DistrictID: 3035, DistrictName: 'Thanh Liêm', Code: '3035' },
      { DistrictID: 3036, DistrictName: 'Bình Lục', Code: '3036' },
    ],
    // Thái Bình (301)
    301: [
      { DistrictID: 3011, DistrictName: 'TP Thái Bình', Code: '3011' },
      { DistrictID: 3012, DistrictName: 'Quỳnh Phụ', Code: '3012' },
      { DistrictID: 3013, DistrictName: 'Hưng Hà', Code: '3013' },
      { DistrictID: 3014, DistrictName: 'Đông Hưng', Code: '3014' },
      { DistrictID: 3015, DistrictName: 'Thái Thụy', Code: '3015' },
    ],
    // Nam Định (305)
    305: [
      { DistrictID: 3051, DistrictName: 'TP Nam Định', Code: '3051' },
      { DistrictID: 3052, DistrictName: 'Mỹ Lộc', Code: '3052' },
      { DistrictID: 3053, DistrictName: 'Vụ Bản', Code: '3053' },
      { DistrictID: 3054, DistrictName: 'Ý Yên', Code: '3054' },
      { DistrictID: 3055, DistrictName: 'Nghĩa Hưng', Code: '3055' },
    ],
    // Ninh Bình (307)
    307: [
      { DistrictID: 3071, DistrictName: 'TP Ninh Bình', Code: '3071' },
      { DistrictID: 3072, DistrictName: 'Tam Điệp', Code: '3072' },
      { DistrictID: 3073, DistrictName: 'Nho Quan', Code: '3073' },
      { DistrictID: 3074, DistrictName: 'Gia Viễn', Code: '3074' },
      { DistrictID: 3075, DistrictName: 'Hoa Lư', Code: '3075' },
    ],
    // Thanh Hóa (309)
    309: [
      { DistrictID: 3091, DistrictName: 'TP Thanh Hóa', Code: '3091' },
      { DistrictID: 3092, DistrictName: 'Bỉm Sơn', Code: '3092' },
      { DistrictID: 3093, DistrictName: 'Sầm Sơn', Code: '3093' },
      { DistrictID: 3094, DistrictName: 'Nghi Sơn', Code: '3094' },
      { DistrictID: 3095, DistrictName: 'Quảng Xương', Code: '3095' },
    ],
    // Nghệ An (311)
    311: [
      { DistrictID: 3111, DistrictName: 'TP Vinh', Code: '3111' },
      { DistrictID: 3112, DistrictName: 'Cửa Lò', Code: '3112' },
      { DistrictID: 3113, DistrictName: 'Thái Hòa', Code: '3113' },
      { DistrictID: 3114, DistrictName: 'Diễn Châu', Code: '3114' },
      { DistrictID: 3115, DistrictName: 'Yên Thành', Code: '3115' },
    ],
    // Hà Tĩnh (313)
    313: [
      { DistrictID: 3131, DistrictName: 'TP Hà Tĩnh', Code: '3131' },
      { DistrictID: 3132, DistrictName: 'Hồng Lĩnh', Code: '3132' },
      { DistrictID: 3133, DistrictName: 'Hương Sơn', Code: '3133' },
      { DistrictID: 3134, DistrictName: 'Đức Thọ', Code: '3134' },
      { DistrictID: 3135, DistrictName: 'Kỳ Anh', Code: '3135' },
    ],
    // Điện Biên (271) - MỚI THÊM
    271: [
      { DistrictID: 2711, DistrictName: 'TP Điện Biên Phủ', Code: '2711' },
      { DistrictID: 2712, DistrictName: 'TX Mường Lay', Code: '2712' },
      { DistrictID: 2713, DistrictName: 'Mường Nhé', Code: '2713' },
      { DistrictID: 2714, DistrictName: 'Mường Chà', Code: '2714' },
      { DistrictID: 2715, DistrictName: 'Tủa Chùa', Code: '2715' },
      { DistrictID: 2716, DistrictName: 'Tuần Giáo', Code: '2716' },
      { DistrictID: 2717, DistrictName: 'Điện Biên', Code: '2717' },
      { DistrictID: 2718, DistrictName: 'Điện Biên Đông', Code: '2718' },
      { DistrictID: 2719, DistrictName: 'Mường Ảng', Code: '2719' },
      { DistrictID: 2720, DistrictName: 'Nậm Pồ', Code: '2720' },
    ],
    // Lào Cai (269) - MỚI THÊM
    269: [
      { DistrictID: 2691, DistrictName: 'TP Lào Cai', Code: '2691' },
      { DistrictID: 2692, DistrictName: 'Bát Xát', Code: '2692' },
      { DistrictID: 2693, DistrictName: 'Mường Khương', Code: '2693' },
      { DistrictID: 2694, DistrictName: 'Si Ma Cai', Code: '2694' },
      { DistrictID: 2695, DistrictName: 'Bắc Hà', Code: '2695' },
      { DistrictID: 2696, DistrictName: 'Bảo Thắng', Code: '2696' },
      { DistrictID: 2697, DistrictName: 'Bảo Yên', Code: '2697' },
      { DistrictID: 2698, DistrictName: 'Sa Pa', Code: '2698' },
      { DistrictID: 2699, DistrictName: 'Văn Bàn', Code: '2699' },
    ],
    // Sơn La (275) - MỚI THÊM
    275: [
      { DistrictID: 2751, DistrictName: 'TP Sơn La', Code: '2751' },
      { DistrictID: 2752, DistrictName: 'Quỳnh Nhai', Code: '2752' },
      { DistrictID: 2753, DistrictName: 'Thuận Châu', Code: '2753' },
      { DistrictID: 2754, DistrictName: 'Mường La', Code: '2754' },
      { DistrictID: 2755, DistrictName: 'Bắc Yên', Code: '2755' },
      { DistrictID: 2756, DistrictName: 'Phù Yên', Code: '2756' },
      { DistrictID: 2757, DistrictName: 'Mộc Châu', Code: '2757' },
      { DistrictID: 2758, DistrictName: 'Yên Châu', Code: '2758' },
      { DistrictID: 2759, DistrictName: 'Mai Sơn', Code: '2759' },
      { DistrictID: 2760, DistrictName: 'Sông Mã', Code: '2760' },
      { DistrictID: 2761, DistrictName: 'Sốp Cộp', Code: '2761' },
    ],
    // Yên Bái (277) - MỚI THÊM
    277: [
      { DistrictID: 2771, DistrictName: 'TP Yên Bái', Code: '2771' },
      { DistrictID: 2772, DistrictName: 'TX Nghĩa Lộ', Code: '2772' },
      { DistrictID: 2773, DistrictName: 'Lục Yên', Code: '2773' },
      { DistrictID: 2774, DistrictName: 'Văn Yên', Code: '2774' },
      { DistrictID: 2775, DistrictName: 'Mù Cang Chải', Code: '2775' },
      { DistrictID: 2776, DistrictName: 'Trấn Yên', Code: '2776' },
      { DistrictID: 2777, DistrictName: 'Trạm Tấu', Code: '2777' },
      { DistrictID: 2778, DistrictName: 'Văn Chấn', Code: '2778' },
      { DistrictID: 2779, DistrictName: 'Yên Bình', Code: '2779' },
    ],
    // Tuyên Quang (386) - MỚI THÊM
    386: [
      { DistrictID: 3861, DistrictName: 'TP Tuyên Quang', Code: '3861' },
      { DistrictID: 3862, DistrictName: 'Lâm Bình', Code: '3862' },
      { DistrictID: 3863, DistrictName: 'Na Hang', Code: '3863' },
      { DistrictID: 3864, DistrictName: 'Chiêm Hóa', Code: '3864' },
      { DistrictID: 3865, DistrictName: 'Hàm Yên', Code: '3865' },
      { DistrictID: 3866, DistrictName: 'Yên Sơn', Code: '3866' },
      { DistrictID: 3867, DistrictName: 'Sơn Dương', Code: '3867' },
    ],
    // Lạng Sơn (283) - MỚI THÊM
    283: [
      { DistrictID: 2831, DistrictName: 'TP Lạng Sơn', Code: '2831' },
      { DistrictID: 2832, DistrictName: 'Tràng Định', Code: '2832' },
      { DistrictID: 2833, DistrictName: 'Bình Gia', Code: '2833' },
      { DistrictID: 2834, DistrictName: 'Văn Lãng', Code: '2834' },
      { DistrictID: 2835, DistrictName: 'Cao Lộc', Code: '2835' },
      { DistrictID: 2836, DistrictName: 'Văn Quan', Code: '2836' },
      { DistrictID: 2837, DistrictName: 'Bắc Sơn', Code: '2837' },
      { DistrictID: 2838, DistrictName: 'Hữu Lũng', Code: '2838' },
      { DistrictID: 2839, DistrictName: 'Chi Lăng', Code: '2839' },
      { DistrictID: 2840, DistrictName: 'Lộc Bình', Code: '2840' },
      { DistrictID: 2841, DistrictName: 'Đình Lập', Code: '2841' },
    ],
    // Cao Bằng (382) - MỚI THÊM
    382: [
      { DistrictID: 3821, DistrictName: 'TP Cao Bằng', Code: '3821' },
      { DistrictID: 3822, DistrictName: 'Bảo Lâm', Code: '3822' },
      { DistrictID: 3823, DistrictName: 'Bảo Lạc', Code: '3823' },
      { DistrictID: 3824, DistrictName: 'Hà Quảng', Code: '3824' },
      { DistrictID: 3825, DistrictName: 'Trùng Khánh', Code: '3825' },
      { DistrictID: 3826, DistrictName: 'Hạ Lang', Code: '3826' },
      { DistrictID: 3827, DistrictName: 'Quảng Hòa', Code: '3827' },
      { DistrictID: 3828, DistrictName: 'Hoà An', Code: '3828' },
      { DistrictID: 3829, DistrictName: 'Nguyên Bình', Code: '3829' },
      { DistrictID: 3830, DistrictName: 'Thạch An', Code: '3830' },
    ],
    // Bắc Kạn (384) - MỚI THÊM
    384: [
      { DistrictID: 3841, DistrictName: 'TP Bắc Kạn', Code: '3841' },
      { DistrictID: 3842, DistrictName: 'Pác Nặm', Code: '3842' },
      { DistrictID: 3843, DistrictName: 'Ba Bể', Code: '3843' },
      { DistrictID: 3844, DistrictName: 'Ngân Sơn', Code: '3844' },
      { DistrictID: 3845, DistrictName: 'Bạch Thông', Code: '3845' },
      { DistrictID: 3846, DistrictName: 'Chợ Đồn', Code: '3846' },
      { DistrictID: 3847, DistrictName: 'Chợ Mới', Code: '3847' },
      { DistrictID: 3848, DistrictName: 'Na Rì', Code: '3848' },
    ],
    // Lai Châu (273) - MỚI THÊM
    273: [
      { DistrictID: 2731, DistrictName: 'TP Lai Châu', Code: '2731' },
      { DistrictID: 2732, DistrictName: 'Tam Đường', Code: '2732' },
      { DistrictID: 2733, DistrictName: 'Mường Tè', Code: '2733' },
      { DistrictID: 2734, DistrictName: 'Sìn Hồ', Code: '2734' },
      { DistrictID: 2735, DistrictName: 'Phong Thổ', Code: '2735' },
      { DistrictID: 2736, DistrictName: 'Than Uyên', Code: '2736' },
      { DistrictID: 2737, DistrictName: 'Tân Uyên', Code: '2737' },
      { DistrictID: 2738, DistrictName: 'Nậm Nhùn', Code: '2738' },
    ],
    // THÊM TẤT CẢ CÁC TỈNH CÒN LẠI
    // Hà Giang (380)
    380: [
      { DistrictID: 3801, DistrictName: 'TP Hà Giang', Code: '3801' },
      { DistrictID: 3802, DistrictName: 'Đồng Văn', Code: '3802' },
      { DistrictID: 3803, DistrictName: 'Mèo Vạc', Code: '3803' },
      { DistrictID: 3804, DistrictName: 'Yên Minh', Code: '3804' },
      { DistrictID: 3805, DistrictName: 'Quản Bạ', Code: '3805' },
    ],
    // Thái Nguyên (281)
    281: [
      { DistrictID: 2811, DistrictName: 'TP Thái Nguyên', Code: '2811' },
      { DistrictID: 2812, DistrictName: 'TP Sông Công', Code: '2812' },
      { DistrictID: 2813, DistrictName: 'Phổ Yên', Code: '2813' },
      { DistrictID: 2814, DistrictName: 'Đại Từ', Code: '2814' },
      { DistrictID: 2815, DistrictName: 'Phú Lương', Code: '2815' },
    ],
    // Hoà Bình (279)
    279: [
      { DistrictID: 2791, DistrictName: 'TP Hòa Bình', Code: '2791' },
      { DistrictID: 2792, DistrictName: 'Đà Bắc', Code: '2792' },
      { DistrictID: 2793, DistrictName: 'Mai Châu', Code: '2793' },
      { DistrictID: 2794, DistrictName: 'Lương Sơn', Code: '2794' },
      { DistrictID: 2795, DistrictName: 'Kim Bôi', Code: '2795' },
    ],
    // Bắc Giang (287)
    287: [
      { DistrictID: 2871, DistrictName: 'TP Bắc Giang', Code: '2871' },
      { DistrictID: 2872, DistrictName: 'Yên Thế', Code: '2872' },
      { DistrictID: 2873, DistrictName: 'Lục Ngạn', Code: '2873' },
      { DistrictID: 2874, DistrictName: 'Lục Nam', Code: '2874' },
      { DistrictID: 2875, DistrictName: 'Sơn Động', Code: '2875' },
    ],
    // Vĩnh Phúc (291)
    291: [
      { DistrictID: 2911, DistrictName: 'TP Vĩnh Yên', Code: '2911' },
      { DistrictID: 2912, DistrictName: 'TP Phúc Yên', Code: '2912' },
      { DistrictID: 2913, DistrictName: 'Bình Xuyên', Code: '2913' },
      { DistrictID: 2914, DistrictName: 'Tam Dương', Code: '2914' },
      { DistrictID: 2915, DistrictName: 'Vĩnh Tường', Code: '2915' },
    ],
    // Bắc Ninh (293)
    293: [
      { DistrictID: 2931, DistrictName: 'TP Bắc Ninh', Code: '2931' },
      { DistrictID: 2932, DistrictName: 'Từ Sơn', Code: '2932' },
      { DistrictID: 2933, DistrictName: 'Thuận Thành', Code: '2933' },
      { DistrictID: 2934, DistrictName: 'Gia Bình', Code: '2934' },
      { DistrictID: 2935, DistrictName: 'Lương Tài', Code: '2935' },
    ],
    // Hải Dương (295)
    295: [
      { DistrictID: 2951, DistrictName: 'TP Hải Dương', Code: '2951' },
      { DistrictID: 2952, DistrictName: 'Chí Linh', Code: '2952' },
      { DistrictID: 2953, DistrictName: 'Nam Sách', Code: '2953' },
      { DistrictID: 2954, DistrictName: 'Kinh Môn', Code: '2954' },
      { DistrictID: 2955, DistrictName: 'Thanh Hà', Code: '2955' },
    ],
    // THÊM CÁC TỈNH MIỀN TRUNG VÀ NAM
    // Quảng Bình (315)
    315: [
      { DistrictID: 3151, DistrictName: 'TP Đồng Hới', Code: '3151' },
      { DistrictID: 3152, DistrictName: 'Bố Trạch', Code: '3152' },
      { DistrictID: 3153, DistrictName: 'Quảng Trạch', Code: '3153' },
      { DistrictID: 3154, DistrictName: 'Quảng Ninh', Code: '3154' },
      { DistrictID: 3155, DistrictName: 'Lệ Thủy', Code: '3155' },
    ],
    // Quảng Trị (317)
    317: [
      { DistrictID: 3171, DistrictName: 'TP Đông Hà', Code: '3171' },
      { DistrictID: 3172, DistrictName: 'Quảng Trị', Code: '3172' },
      { DistrictID: 3173, DistrictName: 'Vĩnh Linh', Code: '3173' },
      { DistrictID: 3174, DistrictName: 'Gio Linh', Code: '3174' },
      { DistrictID: 3175, DistrictName: 'Hải Lăng', Code: '3175' },
    ],
    // Thừa Thiên Huế (319)
    319: [
      { DistrictID: 3191, DistrictName: 'TP Huế', Code: '3191' },
      { DistrictID: 3192, DistrictName: 'Phong Điền', Code: '3192' },
      { DistrictID: 3193, DistrictName: 'Quảng Điền', Code: '3193' },
      { DistrictID: 3194, DistrictName: 'Phú Vang', Code: '3194' },
      { DistrictID: 3195, DistrictName: 'Hương Thủy', Code: '3195' },
    ],
    // Quảng Nam (321)
    321: [
      { DistrictID: 3211, DistrictName: 'TP Tam Kỳ', Code: '3211' },
      { DistrictID: 3212, DistrictName: 'TP Hội An', Code: '3212' },
      { DistrictID: 3213, DistrictName: 'Điện Bàn', Code: '3213' },
      { DistrictID: 3214, DistrictName: 'Đại Lộc', Code: '3214' },
      { DistrictID: 3215, DistrictName: 'Duy Xuyên', Code: '3215' },
    ],
    // Quảng Ngãi (323)
    323: [
      { DistrictID: 3231, DistrictName: 'TP Quảng Ngãi', Code: '3231' },
      { DistrictID: 3232, DistrictName: 'Bình Sơn', Code: '3232' },
      { DistrictID: 3233, DistrictName: 'Sơn Tịnh', Code: '3233' },
      { DistrictID: 3234, DistrictName: 'Tư Nghĩa', Code: '3234' },
      { DistrictID: 3235, DistrictName: 'Nghĩa Hành', Code: '3235' },
    ],
    // Bình Định (325)
    325: [
      { DistrictID: 3251, DistrictName: 'TP Quy Nhơn', Code: '3251' },
      { DistrictID: 3252, DistrictName: 'An Nhơn', Code: '3252' },
      { DistrictID: 3253, DistrictName: 'Hoài Nhơn', Code: '3253' },
      { DistrictID: 3254, DistrictName: 'Tuy Phước', Code: '3254' },
      { DistrictID: 3255, DistrictName: 'Phù Cát', Code: '3255' },
    ],
    // Phú Yên (327)
    327: [
      { DistrictID: 3271, DistrictName: 'TP Tuy Hòa', Code: '3271' },
      { DistrictID: 3272, DistrictName: 'Sông Cầu', Code: '3272' },
      { DistrictID: 3273, DistrictName: 'Đồng Xuân', Code: '3273' },
      { DistrictID: 3274, DistrictName: 'Tuy An', Code: '3274' },
      { DistrictID: 3275, DistrictName: 'Sơn Hòa', Code: '3275' },
    ],
    // Các tỉnh Tây Nguyên
    // Kon Tum (335)
    335: [
      { DistrictID: 3351, DistrictName: 'TP Kon Tum', Code: '3351' },
      { DistrictID: 3352, DistrictName: 'Đăk Glei', Code: '3352' },
      { DistrictID: 3353, DistrictName: 'Đăk Tô', Code: '3353' },
      { DistrictID: 3354, DistrictName: 'Kon Plông', Code: '3354' },
      { DistrictID: 3355, DistrictName: 'Kon Rẫy', Code: '3355' },
    ],
    // Gia Lai (337)
    337: [
      { DistrictID: 3371, DistrictName: 'TP Pleiku', Code: '3371' },
      { DistrictID: 3372, DistrictName: 'An Khê', Code: '3372' },
      { DistrictID: 3373, DistrictName: 'Ayun Pa', Code: '3373' },
      { DistrictID: 3374, DistrictName: 'Chư Păh', Code: '3374' },
      { DistrictID: 3375, DistrictName: 'Chư Prông', Code: '3375' },
    ],
    // Đắk Lắk (339)
    339: [
      { DistrictID: 3391, DistrictName: 'TP Buôn Ma Thuột', Code: '3391' },
      { DistrictID: 3392, DistrictName: 'Buôn Hồ', Code: '3392' },
      { DistrictID: 3393, DistrictName: 'Ea Kar', Code: '3393' },
      { DistrictID: 3394, DistrictName: 'Krông Păk', Code: '3394' },
      { DistrictID: 3395, DistrictName: 'Krông Búk', Code: '3395' },
    ],
    // Đắk Nông (341)
    341: [
      { DistrictID: 3411, DistrictName: 'TP Gia Nghĩa', Code: '3411' },
      { DistrictID: 3412, DistrictName: 'Đăk Glong', Code: '3412' },
      { DistrictID: 3413, DistrictName: 'Cư Jút', Code: '3413' },
      { DistrictID: 3414, DistrictName: 'Đăk Mil', Code: '3414' },
      { DistrictID: 3415, DistrictName: 'Krông Nô', Code: '3415' },
    ],
    // Lâm Đồng (343)
    343: [
      { DistrictID: 3431, DistrictName: 'TP Đà Lạt', Code: '3431' },
      { DistrictID: 3432, DistrictName: 'TP Bảo Lộc', Code: '3432' },
      { DistrictID: 3433, DistrictName: 'Đơn Dương', Code: '3433' },
      { DistrictID: 3434, DistrictName: 'Đức Trọng', Code: '3434' },
      { DistrictID: 3435, DistrictName: 'Lạc Dương', Code: '3435' },
    ],
    // Các tỉnh Đông Nam Bộ
    // Bình Phước (345)
    345: [
      { DistrictID: 3451, DistrictName: 'TP Đồng Xoài', Code: '3451' },
      { DistrictID: 3452, DistrictName: 'Bù Đăng', Code: '3452' },
      { DistrictID: 3453, DistrictName: 'Chơn Thành', Code: '3453' },
      { DistrictID: 3454, DistrictName: 'Bù Đốp', Code: '3454' },
      { DistrictID: 3455, DistrictName: 'Phước Long', Code: '3455' },
    ],
    // Tây Ninh (347)
    347: [
      { DistrictID: 3471, DistrictName: 'TP Tây Ninh', Code: '3471' },
      { DistrictID: 3472, DistrictName: 'Tân Biên', Code: '3472' },
      { DistrictID: 3473, DistrictName: 'Tân Châu', Code: '3473' },
      { DistrictID: 3474, DistrictName: 'Dương Minh Châu', Code: '3474' },
      { DistrictID: 3475, DistrictName: 'Châu Thành', Code: '3475' },
    ],
    // Ninh Thuận (331)
    331: [
      { DistrictID: 3311, DistrictName: 'TP Phan Rang-Tháp Chàm', Code: '3311' },
      { DistrictID: 3312, DistrictName: 'Ninh Sơn', Code: '3312' },
      { DistrictID: 3313, DistrictName: 'Ninh Hải', Code: '3313' },
      { DistrictID: 3314, DistrictName: 'Ninh Phước', Code: '3314' },
      { DistrictID: 3315, DistrictName: 'Thuận Bắc', Code: '3315' },
    ],
    // Bình Thuận (333)
    333: [
      { DistrictID: 3331, DistrictName: 'TP Phan Thiết', Code: '3331' },
      { DistrictID: 3332, DistrictName: 'La Gi', Code: '3332' },
      { DistrictID: 3333, DistrictName: 'Tuy Phong', Code: '3333' },
      { DistrictID: 3334, DistrictName: 'Bắc Bình', Code: '3334' },
      { DistrictID: 3335, DistrictName: 'Hàm Thuận Bắc', Code: '3335' },
    ],
    // Các tỉnh ĐBSCL
    // Long An (355)
    355: [
      { DistrictID: 3551, DistrictName: 'TP Tân An', Code: '3551' },
      { DistrictID: 3552, DistrictName: 'Bến Lức', Code: '3552' },
      { DistrictID: 3553, DistrictName: 'Cần Đước', Code: '3553' },
      { DistrictID: 3554, DistrictName: 'Cần Giuộc', Code: '3554' },
      { DistrictID: 3555, DistrictName: 'Châu Thành', Code: '3555' },
    ],
    // Tiền Giang (357)
    357: [
      { DistrictID: 3571, DistrictName: 'TP Mỹ Tho', Code: '3571' },
      { DistrictID: 3572, DistrictName: 'Gò Công', Code: '3572' },
      { DistrictID: 3573, DistrictName: 'Cai Lậy', Code: '3573' },
      { DistrictID: 3574, DistrictName: 'Châu Thành', Code: '3574' },
      { DistrictID: 3575, DistrictName: 'Cái Bè', Code: '3575' },
    ],
    // Bến Tre (359)
    359: [
      { DistrictID: 3591, DistrictName: 'TP Bến Tre', Code: '3591' },
      { DistrictID: 3592, DistrictName: 'Châu Thành', Code: '3592' },
      { DistrictID: 3593, DistrictName: 'Chợ Lách', Code: '3593' },
      { DistrictID: 3594, DistrictName: 'Mỏ Cày Bắc', Code: '3594' },
      { DistrictID: 3595, DistrictName: 'Giồng Trôm', Code: '3595' },
    ],
    // Trà Vinh (361)
    361: [
      { DistrictID: 3611, DistrictName: 'TP Trà Vinh', Code: '3611' },
      { DistrictID: 3612, DistrictName: 'Càng Long', Code: '3612' },
      { DistrictID: 3613, DistrictName: 'Cầu Kè', Code: '3613' },
      { DistrictID: 3614, DistrictName: 'Tiểu Cần', Code: '3614' },
      { DistrictID: 3615, DistrictName: 'Châu Thành', Code: '3615' },
    ],
    // Vĩnh Long (363)
    363: [
      { DistrictID: 3631, DistrictName: 'TP Vĩnh Long', Code: '3631' },
      { DistrictID: 3632, DistrictName: 'Bình Minh', Code: '3632' },
      { DistrictID: 3633, DistrictName: 'Long Hồ', Code: '3633' },
      { DistrictID: 3634, DistrictName: 'Mang Thít', Code: '3634' },
      { DistrictID: 3635, DistrictName: 'Tam Bình', Code: '3635' },
    ],
    // Đồng Tháp (365)
    365: [
      { DistrictID: 3651, DistrictName: 'TP Cao Lãnh', Code: '3651' },
      { DistrictID: 3652, DistrictName: 'TP Sa Đéc', Code: '3652' },
      { DistrictID: 3653, DistrictName: 'Hồng Ngự', Code: '3653' },
      { DistrictID: 3654, DistrictName: 'Tân Hồng', Code: '3654' },
      { DistrictID: 3655, DistrictName: 'Tam Nông', Code: '3655' },
    ],
    // An Giang (367)
    367: [
      { DistrictID: 3671, DistrictName: 'TP Long Xuyên', Code: '3671' },
      { DistrictID: 3672, DistrictName: 'TP Châu Đốc', Code: '3672' },
      { DistrictID: 3673, DistrictName: 'Châu Phú', Code: '3673' },
      { DistrictID: 3674, DistrictName: 'Tịnh Biên', Code: '3674' },
      { DistrictID: 3675, DistrictName: 'Tri Tôn', Code: '3675' },
    ],
    // Kiên Giang (369)
    369: [
      { DistrictID: 3691, DistrictName: 'TP Rạch Giá', Code: '3691' },
      { DistrictID: 3692, DistrictName: 'TP Hà Tiên', Code: '3692' },
      { DistrictID: 3693, DistrictName: 'Kiên Lương', Code: '3693' },
      { DistrictID: 3694, DistrictName: 'Hòn Đất', Code: '3694' },
      { DistrictID: 3695, DistrictName: 'Phú Quốc', Code: '3695' },
    ],
    // Hậu Giang (371)
    371: [
      { DistrictID: 3711, DistrictName: 'TP Vị Thanh', Code: '3711' },
      { DistrictID: 3712, DistrictName: 'TX Ngã Bảy', Code: '3712' },
      { DistrictID: 3713, DistrictName: 'Châu Thành', Code: '3713' },
      { DistrictID: 3714, DistrictName: 'Châu Thành A', Code: '3714' },
      { DistrictID: 3715, DistrictName: 'Long Mỹ', Code: '3715' },
    ],
    // Sóc Trăng (373)
    373: [
      { DistrictID: 3731, DistrictName: 'TP Sóc Trăng', Code: '3731' },
      { DistrictID: 3732, DistrictName: 'Châu Thành', Code: '3732' },
      { DistrictID: 3733, DistrictName: 'Kế Sách', Code: '3733' },
      { DistrictID: 3734, DistrictName: 'Mỹ Tú', Code: '3734' },
      { DistrictID: 3735, DistrictName: 'Cù Lao Dung', Code: '3735' },
    ],
    // Bạc Liêu (375)
    375: [
      { DistrictID: 3751, DistrictName: 'TP Bạc Liêu', Code: '3751' },
      { DistrictID: 3752, DistrictName: 'Hòa Bình', Code: '3752' },
      { DistrictID: 3753, DistrictName: 'Giá Rai', Code: '3753' },
      { DistrictID: 3754, DistrictName: 'Đông Hải', Code: '3754' },
      { DistrictID: 3755, DistrictName: 'Hồng Dân', Code: '3755' },
    ],
    // Cà Mau (377)
    377: [
      { DistrictID: 3771, DistrictName: 'TP Cà Mau', Code: '3771' },
      { DistrictID: 3772, DistrictName: 'U Minh', Code: '3772' },
      { DistrictID: 3773, DistrictName: 'Thới Bình', Code: '3773' },
      { DistrictID: 3774, DistrictName: 'Trần Văn Thời', Code: '3774' },
      { DistrictID: 3775, DistrictName: 'Cái Nước', Code: '3775' },
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
    ],
    3013: [ // Hưng Hà - Thái Bình
      { WardCode: '301301', WardName: 'Thị trấn Hưng Hà' },
      { WardCode: '301302', WardName: 'Xã Điệp Nông' },
      { WardCode: '301303', WardName: 'Xã Tân Lễ' },
      { WardCode: '301304', WardName: 'Xã Cộng Hòa' },
      { WardCode: '301305', WardName: 'Xã Dân Chủ' },
      { WardCode: '301306', WardName: 'Xã Canh Tân' },
      { WardCode: '301307', WardName: 'Xã Hòa Tiến' },
      { WardCode: '301308', WardName: 'Xã Hùng Dũng' },
    ],
    3011: [ // TP Thái Bình
      { WardCode: '301101', WardName: 'Phường Trần Hưng Đạo' },
      { WardCode: '301102', WardName: 'Phường Phú Khánh' },
      { WardCode: '301103', WardName: 'Phường Tiền Phong' },
      { WardCode: '301104', WardName: 'Phường Trần Lãm' },
    ],
    3012: [ // Quỳnh Phụ - Thái Bình
      { WardCode: '301201', WardName: 'Thị trấn Quỳnh Côi' },
      { WardCode: '301202', WardName: 'Xã An Khê' },
      { WardCode: '301203', WardName: 'Xã An Đồng' },
      { WardCode: '301204', WardName: 'Xã Quỳnh Hoa' },
    ],
    3014: [ // Đông Hưng - Thái Bình
      { WardCode: '301401', WardName: 'Thị trấn Đông Hưng' },
      { WardCode: '301402', WardName: 'Xã Đông Phương' },
      { WardCode: '301403', WardName: 'Xã Liên Giang' },
      { WardCode: '301404', WardName: 'Xã Đông Sơn' },
    ],
    3015: [ // Thái Thụy - Thái Bình
      { WardCode: '301501', WardName: 'Thị trấn Diêm Điền' },
      { WardCode: '301502', WardName: 'Xã Thụy Trường' },
      { WardCode: '301503', WardName: 'Xã Thụy Dương' },
      { WardCode: '301504', WardName: 'Xã Thụy Hải' },
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
    // TP Việt Trì - Phú Thọ - MỚI THÊM
    2891: [
      { WardCode: '289101', WardName: 'Phường Dữu Lâu' },
      { WardCode: '289102', WardName: 'Phường Vân Cơ' },
      { WardCode: '289103', WardName: 'Phường Nông Trang' },
      { WardCode: '289104', WardName: 'Phường Tân Dân' },
      { WardCode: '289105', WardName: 'Phường Gia Cẩm' },
    ],
    2892: [ // TX Phú Thọ - Phú Thọ
      { WardCode: '289201', WardName: 'Phường Hùng Vương' },
      { WardCode: '289202', WardName: 'Phường Thanh Miếu' },
      { WardCode: '289203', WardName: 'Phường Trưng Vương' },
      { WardCode: '289204', WardName: 'Phường Phong Châu' },
    ],
    2893: [ // Đoan Hùng - Phú Thọ
      { WardCode: '289301', WardName: 'Thị trấn Đoan Hùng' },
      { WardCode: '289302', WardName: 'Xã Hùng Xuyên' },
      { WardCode: '289303', WardName: 'Xã Vân Du' },
      { WardCode: '289304', WardName: 'Xã Phú Lâm' },
    ],
    // TP Hưng Yên - Hưng Yên - MỚI THÊM
    2991: [
      { WardCode: '299101', WardName: 'Phường Hiến Nam' },
      { WardCode: '299102', WardName: 'Phường An Tảo' },
      { WardCode: '299103', WardName: 'Phường Lê Lợi' },
      { WardCode: '299104', WardName: 'Phường Minh Khai' },
      { WardCode: '299105', WardName: 'Phường Quang Trung' },
    ],
    2992: [ // Văn Lâm - Hưng Yên
      { WardCode: '299201', WardName: 'Thị trấn Như Quỳnh' },
      { WardCode: '299202', WardName: 'Xã Lạc Đạo' },
      { WardCode: '299203', WardName: 'Xã Chỉ Đạo' },
      { WardCode: '299204', WardName: 'Xã Đại Đồng' },
    ],
    2993: [ // Văn Giang - Hưng Yên
      { WardCode: '299301', WardName: 'Thị trấn Văn Giang' },
      { WardCode: '299302', WardName: 'Xã Xuân Quan' },
      { WardCode: '299303', WardName: 'Xã Cửu Cao' },
      { WardCode: '299304', WardName: 'Xã Phụng Công' },
    ],
    2994: [ // Yên Mỹ - Hưng Yên
      { WardCode: '299401', WardName: 'Thị trấn Yên Mỹ' },
      { WardCode: '299402', WardName: 'Xã Giai Phạm' },
      { WardCode: '299403', WardName: 'Xã Nghĩa Hiệp' },
      { WardCode: '299404', WardName: 'Xã Đồng Than' },
    ],
    2995: [ // Mỹ Hào - Hưng Yên
      { WardCode: '299501', WardName: 'Thị trấn Mỹ Hào' },
      { WardCode: '299502', WardName: 'Xã Bần Yên Nhân' },
      { WardCode: '299503', WardName: 'Xã Dương Quang' },
      { WardCode: '299504', WardName: 'Xã Hòa Phong' },
    ],
    // TP Phủ Lý - Hà Nam
    3031: [
      { WardCode: '303101', WardName: 'Phường Quang Trung' },
      { WardCode: '303102', WardName: 'Phường Lương Khánh Thiện' },
      { WardCode: '303103', WardName: 'Phường Lê Hồng Phong' },
      { WardCode: '303104', WardName: 'Phường Minh Khai' },
      { WardCode: '303105', WardName: 'Phường Hai Bà Trưng' },
    ],
    3032: [ // Duy Tiên - Hà Nam
      { WardCode: '303201', WardName: 'Thị trấn Đồng Văn' },
      { WardCode: '303202', WardName: 'Xã Châu Giang' },
      { WardCode: '303203', WardName: 'Xã Mộc Bắc' },
      { WardCode: '303204', WardName: 'Xã Tiền Phong' },
    ],
    3033: [ // Kim Bảng - Hà Nam
      { WardCode: '303301', WardName: 'Thị trấn Ba Sao' },
      { WardCode: '303302', WardName: 'Xã Đồng Hóa' },
      { WardCode: '303303', WardName: 'Xã Nguyễn Úy' },
      { WardCode: '303304', WardName: 'Xã Khả Phong' },
    ],
    // TP Thái Bình - Thái Bình
    3011: [
      { WardCode: '301101', WardName: 'Phường Trần Hưng Đạo' },
      { WardCode: '301102', WardName: 'Phường Trần Lãm' },
      { WardCode: '301103', WardName: 'Phường Đề Thám' },
      { WardCode: '301104', WardName: 'Phường Phú Khánh' },
    ],
    // TP Nam Định - Nam Định
    3051: [
      { WardCode: '305101', WardName: 'Phường Trần Đăng Ninh' },
      { WardCode: '305102', WardName: 'Phường Lộc Hạ' },
      { WardCode: '305103', WardName: 'Phường Lộc Vượng' },
      { WardCode: '305104', WardName: 'Phường Cửa Nam' },
    ],
    // TP Ninh Bình - Ninh Bình
    3071: [
      { WardCode: '307101', WardName: 'Phường Đông Thành' },
      { WardCode: '307102', WardName: 'Phường Tân Thành' },
      { WardCode: '307103', WardName: 'Phường Nam Thành' },
      { WardCode: '307104', WardName: 'Phường Bắc Thành' },
    ],
    // TP Thanh Hóa - Thanh Hóa
    3091: [
      { WardCode: '309101', WardName: 'Phường Điện Biên' },
      { WardCode: '309102', WardName: 'Phường Lam Sơn' },
      { WardCode: '309103', WardName: 'Phường Đông Hương' },
      { WardCode: '309104', WardName: 'Phường Trường Thi' },
    ],
    // TP Vinh - Nghệ An
    3111: [
      { WardCode: '311101', WardName: 'Phường Hà Huy Tập' },
      { WardCode: '311102', WardName: 'Phường Lê Lợi' },
      { WardCode: '311103', WardName: 'Phường Quang Trung' },
      { WardCode: '311104', WardName: 'Phường Đội Cung' },
    ],
    // TP Hà Tĩnh - Hà Tĩnh
    3131: [
      { WardCode: '313101', WardName: 'Phường Trần Phú' },
      { WardCode: '313102', WardName: 'Phường Nam Hà' },
      { WardCode: '313103', WardName: 'Phường Bắc Hà' },
      { WardCode: '313104', WardName: 'Phường Nguyễn Du' },
    ],
    // TP Điện Biên Phủ - Điện Biên - MỚI THÊM
    2711: [
      { WardCode: '271101', WardName: 'Phường Noong Bua' },
      { WardCode: '271102', WardName: 'Phường Him Lam' },
      { WardCode: '271103', WardName: 'Phường Thanh Bình' },
      { WardCode: '271104', WardName: 'Phường Tân Thanh' },
      { WardCode: '271105', WardName: 'Phường Mường Thanh' },
      { WardCode: '271106', WardName: 'Phường Nam Thanh' },
      { WardCode: '271107', WardName: 'Phường Thanh Trường' },
    ],
    2712: [ // TX Mường Lay - Điện Biên
      { WardCode: '271201', WardName: 'Phường Sông Đà' },
      { WardCode: '271202', WardName: 'Phường Na Lay' },
      { WardCode: '271203', WardName: 'Xã Lay Nưa' },
    ],
    2713: [ // Mường Nhé - Điện Biên
      { WardCode: '271301', WardName: 'Thị trấn Mường Nhé' },
      { WardCode: '271302', WardName: 'Xã Sín Thầu' },
      { WardCode: '271303', WardName: 'Xã Ma Thì Hồ' },
      { WardCode: '271304', WardName: 'Xã Nậm Vì' },
    ],
    2714: [ // Mường Chà - Điện Biên
      { WardCode: '271401', WardName: 'Thị trấn Mường Chà' },
      { WardCode: '271402', WardName: 'Xã Xá Tổng' },
      { WardCode: '271403', WardName: 'Xã Mường Tùng' },
      { WardCode: '271404', WardName: 'Xã Hừa Ngài' },
    ],
    2715: [ // Tủa Chùa - Điện Biên
      { WardCode: '271501', WardName: 'Thị trấn Tủa Chùa' },
      { WardCode: '271502', WardName: 'Xã Tủa Thàng' },
      { WardCode: '271503', WardName: 'Xã Trung Thu' },
      { WardCode: '271504', WardName: 'Xã Sinh Phình' },
    ],
    2716: [ // Tuần Giáo - Điện Biên
      { WardCode: '271601', WardName: 'Thị trấn Tuần Giáo' },
      { WardCode: '271602', WardName: 'Xã Phình Sáng' },
      { WardCode: '271603', WardName: 'Xã Rạng Đông' },
      { WardCode: '271604', WardName: 'Xã Mùn Chung' },
    ],
    // TP Lào Cai - Lào Cai - MỚI THÊM
    2691: [
      { WardCode: '269101', WardName: 'Phường Duyên Hải' },
      { WardCode: '269102', WardName: 'Phường Lào Cai' },
      { WardCode: '269103', WardName: 'Phường Cốc Lếu' },
      { WardCode: '269104', WardName: 'Phường Kim Tân' },
      { WardCode: '269105', WardName: 'Phường Bắc Lệnh' },
      { WardCode: '269106', WardName: 'Phường Pom Hán' },
      { WardCode: '269107', WardName: 'Phường Xuân Tăng' },
    ],
    2698: [ // Sa Pa - Lào Cai
      { WardCode: '269801', WardName: 'TT Sa Pa' },
      { WardCode: '269802', WardName: 'Xã Tả Phìn' },
      { WardCode: '269803', WardName: 'Xã Lao Chải' },
      { WardCode: '269804', WardName: 'Xã Tả Van' },
      { WardCode: '269805', WardName: 'Xã Hầu Thào' },
    ],
    // TP Sơn La - Sơn La - MỚI THÊM
    2751: [
      { WardCode: '275101', WardName: 'Phường Chiềng Lề' },
      { WardCode: '275102', WardName: 'Phường Tô Hiệu' },
      { WardCode: '275103', WardName: 'Phường Quyết Thắng' },
      { WardCode: '275104', WardName: 'Phường Quyết Tâm' },
      { WardCode: '275105', WardName: 'Xã Chiềng Cọ' },
      { WardCode: '275106', WardName: 'Xã Chiềng Đen' },
    ],
    2757: [ // Mộc Châu - Sơn La
      { WardCode: '275701', WardName: 'TT Mộc Châu' },
      { WardCode: '275702', WardName: 'Xã Chiềng Sơn' },
      { WardCode: '275703', WardName: 'Xã Tân Hợp' },
      { WardCode: '275704', WardName: 'Xã Phiêng Luông' },
    ],
    // TP Yên Bái - Yên Bái - MỚI THÊM
    2771: [
      { WardCode: '277101', WardName: 'Phường Yên Thịnh' },
      { WardCode: '277102', WardName: 'Phường Yên Ninh' },
      { WardCode: '277103', WardName: 'Phường Minh Tân' },
      { WardCode: '277104', WardName: 'Phường Nguyễn Thái Học' },
      { WardCode: '277105', WardName: 'Phường Đồng Tâm' },
      { WardCode: '277106', WardName: 'Phường Nguyễn Phúc' },
    ],
    2775: [ // Mù Cang Chải - Yên Bái  
      { WardCode: '277501', WardName: 'TT Mù Cang Chải' },
      { WardCode: '277502', WardName: 'Xã Khao Mang' },
      { WardCode: '277503', WardName: 'Xã Mồ Dề' },
      { WardCode: '277504', WardName: 'Xã Chế Tạo' },
    ],
    // TP Tuyên Quang - Tuyên Quang - MỚI THÊM
    3861: [
      { WardCode: '386101', WardName: 'Phường Phan Thiết' },
      { WardCode: '386102', WardName: 'Phường Minh Xuân' },
      { WardCode: '386103', WardName: 'Phường Tân Quang' },
      { WardCode: '386104', WardName: 'Phường Nông Tiến' },
      { WardCode: '386105', WardName: 'Phường Ỷ La' },
      { WardCode: '386106', WardName: 'Xã Tràng Đà' },
    ],
    // TP Lạng Sơn - Lạng Sơn - MỚI THÊM
    2831: [
      { WardCode: '283101', WardName: 'Phường Chi Lăng' },
      { WardCode: '283102', WardName: 'Phường Hoàng Văn Thụ' },
      { WardCode: '283103', WardName: 'Phường Tam Thanh' },
      { WardCode: '283104', WardName: 'Phường Vĩnh Trại' },
      { WardCode: '283105', WardName: 'Phường Đông Kinh' },
      { WardCode: '283106', WardName: 'Xã Quảng Lạc' },
    ],
    // TP Cao Bằng - Cao Bằng - MỚI THÊM
    3821: [
      { WardCode: '382101', WardName: 'Phường Sông Hiến' },
      { WardCode: '382102', WardName: 'Phường Sông Bằng' },
      { WardCode: '382103', WardName: 'Phường Hợp Giang' },
      { WardCode: '382104', WardName: 'Phường Tân Giang' },
      { WardCode: '382105', WardName: 'Phường Ngọc Xuân' },
      { WardCode: '382106', WardName: 'Xã Vĩnh Quang' },
      { WardCode: '382107', WardName: 'Xã Hưng Đạo' },
      { WardCode: '382108', WardName: 'Xã Chu Trinh' },
    ],
    3822: [ // Bảo Lâm - Cao Bằng
      { WardCode: '382201', WardName: 'Thị trấn Pác Miầu' },
      { WardCode: '382202', WardName: 'Xã Đức Hạnh' },
      { WardCode: '382203', WardName: 'Xã Lý Bôn' },
      { WardCode: '382204', WardName: 'Xã Nam Cao' },
      { WardCode: '382205', WardName: 'Xã Vĩnh Quang' },
    ],
    3823: [ // Bảo Lạc - Cao Bằng
      { WardCode: '382301', WardName: 'Thị trấn Bảo Lạc' },
      { WardCode: '382302', WardName: 'Xã Cốc Pàng' },
      { WardCode: '382303', WardName: 'Xã Thượng Hà' },
      { WardCode: '382304', WardName: 'Xã Cô Ba' },
      { WardCode: '382305', WardName: 'Xã Hưng Đạo' },
    ],
    3824: [ // Hà Quảng - Cao Bằng
      { WardCode: '382401', WardName: 'Thị trấn Xuân Hòa' },
      { WardCode: '382402', WardName: 'Xã Quý Quân' },
      { WardCode: '382403', WardName: 'Xã Hạ Thành' },
      { WardCode: '382404', WardName: 'Xã Tân Việt' },
    ],
    3825: [ // Trùng Khánh - Cao Bằng
      { WardCode: '382501', WardName: 'Thị trấn Trùng Khánh' },
      { WardCode: '382502', WardName: 'Xã Ngọc Khê' },
      { WardCode: '382503', WardName: 'Xã Ngọc Côn' },
      { WardCode: '382504', WardName: 'Xã Phong Nậm' },
    ],
    // TP Bắc Kạn - Bắc Kạn - MỚI THÊM
    3841: [
      { WardCode: '384101', WardName: 'Phường Nguyễn Thị Minh Khai' },
      { WardCode: '384102', WardName: 'Phường Sông Cầu' },
      { WardCode: '384103', WardName: 'Phường Đức Xuân' },
      { WardCode: '384104', WardName: 'Phường Phùng Chí Kiên' },
      { WardCode: '384105', WardName: 'Phường Huyền Tụng' },
      { WardCode: '384106', WardName: 'Xã Dương Quang' },
    ],
    // TP Lai Châu - Lai Châu - MỚI THÊM
    2731: [
      { WardCode: '273101', WardName: 'Phường Quyết Thắng' },
      { WardCode: '273102', WardName: 'Phường Quyết Tiến' },
      { WardCode: '273103', WardName: 'Phường Đoàn Kết' },
      { WardCode: '273104', WardName: 'Phường Tân Phong' },
      { WardCode: '273105', WardName: 'Xã Sùng Phài' },
    ],
    2732: [ // Tam Đường - Lai Châu
      { WardCode: '273201', WardName: 'Thị trấn Tam Đường' },
      { WardCode: '273202', WardName: 'Xã Hồ Thầu' },
      { WardCode: '273203', WardName: 'Xã Bình Lư' },
      { WardCode: '273204', WardName: 'Xã Giang Ma' },
      { WardCode: '273205', WardName: 'Xã Thèn Sin' },
    ],
    2733: [ // Mường Tè - Lai Châu
      { WardCode: '273301', WardName: 'Thị trấn Mường Tè' },
      { WardCode: '273302', WardName: 'Xã Mường Tè' },
      { WardCode: '273303', WardName: 'Xã Thu Lũm' },
      { WardCode: '273304', WardName: 'Xã Ka Lăng' },
      { WardCode: '273305', WardName: 'Xã Tá Bạ' },
    ],
    2734: [ // Sìn Hồ - Lai Châu
      { WardCode: '273401', WardName: 'Thị trấn Sìn Hồ' },
      { WardCode: '273402', WardName: 'Xã Chăn Nưa' },
      { WardCode: '273403', WardName: 'Xã Pa Tần' },
      { WardCode: '273404', WardName: 'Xã Phìn Hồ' },
      { WardCode: '273405', WardName: 'Xã Hồng Thu' },
    ],
    2735: [ // Phong Thổ - Lai Châu
      { WardCode: '273501', WardName: 'Thị trấn Phong Thổ' },
      { WardCode: '273502', WardName: 'Xã Sì Lở Lầu' },
      { WardCode: '273503', WardName: 'Xã Mồ Sì San' },
      { WardCode: '273504', WardName: 'Xã Pa Vây Sử' },
      { WardCode: '273505', WardName: 'Xã Vàng Ma Chải' },
    ],
    2736: [ // Than Uyên - Lai Châu
      { WardCode: '273601', WardName: 'Thị trấn Than Uyên' },
      { WardCode: '273602', WardName: 'Xã Phúc Than' },
      { WardCode: '273603', WardName: 'Xã Mường Than' },
      { WardCode: '273604', WardName: 'Xã Tà Hừa' },
      { WardCode: '273605', WardName: 'Xã Pha Mu' },
    ],
    2737: [ // Tân Uyên - Lai Châu
      { WardCode: '273701', WardName: 'Thị trấn Tân Uyên' },
      { WardCode: '273702', WardName: 'Xã Mường Khoa' },
      { WardCode: '273703', WardName: 'Xã Phúc Khoa' },
      { WardCode: '273704', WardName: 'Xã Thân Thuộc' },
      { WardCode: '273705', WardName: 'Xã Hố Mít' },
    ],
    2738: [ // Nậm Nhùn - Lai Châu
      { WardCode: '273801', WardName: 'Thị trấn Nậm Nhùn' },
      { WardCode: '273802', WardName: 'Xã Hua Bun' },
      { WardCode: '273803', WardName: 'Xã Mường Mô' },
      { WardCode: '273804', WardName: 'Xã Nậm Ban' },
      { WardCode: '273805', WardName: 'Xã Lê Lợi' },
    ],
  }
}

// Flag để bật/tắt mock mode
// ⚠️ Đổi thành false khi backend đã cấu hình GHN Token
// 🎯 MOCK MODE SWITCH
// ═══════════════════════════════════════════════════════════════
// ⚠️ ĐỂ DÙNG API THẬT CỦA GHN:
//    1. Đăng ký tài khoản tại: https://sso.ghn.vn/
//    2. Lấy Token & Shop ID từ https://dev.ghn.vn/
//    3. Cập nhật file: auro/src/main/resources/application.properties
//       - ghn.api.token=<YOUR_TOKEN>
//       - ghn.shop.id=<YOUR_SHOP_ID>
//    4. Đổi USE_MOCK_DATA = false ở dưới
//    5. Restart backend Spring Boot
// ═══════════════════════════════════════════════════════════════
const USE_MOCK_DATA = true // 🔴 true = Mock | 🟢 false = Real GHN API

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
        
        // ⚠️ Cảnh báo nếu không có data cho tỉnh này
        if (districts.value.length === 0) {
          console.warn(`⚠️ [MOCK] Không có dữ liệu quận/huyện cho tỉnh ${provinceId}`)
          
          const provinceName = provinces.value.find(p => p.ProvinceID === provinceId)?.ProvinceName || 'này'
          errors.districts = `
📍 Tỉnh "${provinceName}" chưa có dữ liệu trong Mock Mode.

✅ Các tỉnh có đầy đủ quận/huyện trong Mock:
   • Miền Bắc: Hà Nội, Hải Phòng, Hưng Yên, Phú Thọ, Hà Nam, Thái Bình, Nam Định, Ninh Bình, Thanh Hóa, Nghệ An, Hà Tĩnh
   • Tây Bắc: Điện Biên, Lào Cai, Lai Châu, Sơn La, Yên Bái
   • Miền Trung: Đà Nẵng, Quảng Ninh, Khánh Hòa
   • Miền Nam: TP.HCM, Cần Thơ, Bình Dương, Đồng Nai, Bà Rịa-Vũng Tàu

🚀 Để có TẤT CẢ 63 tỉnh/thành với 100% quận/huyện/xã/phường:
   1. Cấu hình GHN Token → Xem file CAU_HINH_GHN_TOKEN.md
   2. Tắt Mock Mode → Đổi USE_MOCK_DATA = false
   3. Hoặc chọn một trong các tỉnh có sẵn ở trên
          `.trim()
        } else {
          console.log('✅ [MOCK] Loaded districts for province', provinceId, ':', districts.value.length)
        }
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
        
        // 💰 CÔNG THỨC PHÍ SHIP LINH ĐỘNG - TỰ ĐỘNG CHO TẤT CẢ TỈNH
        let baseFee = 20000 // Base fee mặc định
        
        // 1. Phí theo tỉnh (distance fee) - TỰ ĐỘNG
        let provinceFee = 0
        const provinceName = provinces.value.find(p => p.ProvinceID === selectedProvince.value)?.ProvinceName || ''
        
        // Các thành phố lớn (miễn phí)
        const bigCities = [202, 201, 203, 204] // HCM, HN, ĐN, CT
        if (bigCities.includes(selectedProvince.value)) {
          provinceFee = 0
        }
        // Miền Bắc (trừ HN)
        else if ([269, 271, 273, 275, 277, 279, 281, 283, 285, 287, 289, 291, 293, 295, 297, 299, 301, 303, 305, 307, 380, 382, 384, 386].includes(selectedProvince.value)) {
          provinceFee = 30000 // 30k cho miền Bắc
        }
        // Miền Trung
        else if ([309, 311, 313, 315, 317, 319, 321, 323, 325, 327, 329, 331, 333].includes(selectedProvince.value)) {
          provinceFee = 40000 // 40k cho miền Trung
        }
        // Tây Nguyên
        else if ([335, 337, 339, 341, 343].includes(selectedProvince.value)) {
          provinceFee = 45000 // 45k cho Tây Nguyên (xa & núi)
        }
        // Miền Nam (trừ HCM)
        else if ([345, 347, 349, 351, 353, 355, 357, 359, 361, 363, 365, 367, 369, 371, 373, 375, 377].includes(selectedProvince.value)) {
          provinceFee = 25000 // 25k cho miền Nam
        }
        else {
          provinceFee = 35000 // Default cho tỉnh khác
        }
        
        // 2. Phí theo quận (zone fee) - TỰ ĐỘNG theo tên
        let districtFee = 0
        const districtName = districts.value.find(d => d.DistrictID === selectedDistrict.value)?.DistrictName || ''
        
        // Quận trung tâm TP (miễn phí)
        if (districtName.includes('Quận 1') || districtName.includes('Hoàn Kiếm') || districtName.includes('Hải Châu') || districtName.includes('Ninh Kiều')) {
          districtFee = 0
        }
        // Quận nội thành khác
        else if (districtName.includes('Quận') || districtName.startsWith('TP ')) {
          districtFee = 5000
        }
        // Huyện xa
        else if (districtName.includes('Huyện') || districtName.includes('TX ')) {
          districtFee = 10000
        }
        else {
          districtFee = 8000 // Default
        }
        
        // 🆕 3. Phí theo xã/phường - LINH ĐỘNG THEO ĐỘ XA (Distance-based)
        let wardFee = 0
        const wardName = wards.value.find(w => w.WardCode === selectedWard.value)?.WardName || ''
        const wardIndex = wards.value.findIndex(w => w.WardCode === selectedWard.value)
        const totalWards = wards.value.length
        
        // Tính hệ số khoảng cách dựa trên vị trí trong danh sách
        // Xã đầu tiên = gần trung tâm, xã cuối = xa nhất
        const distanceRatio = totalWards > 1 ? wardIndex / (totalWards - 1) : 0
        
        // PHƯỜNG THÀNH PHỐ (gần nhất)
        if (wardName.includes('Phường') && !wardName.includes('Xã')) {
          // Phường trung tâm VIP (số thấp hoặc tên đặc biệt)
          if (wardName.includes('Bến Nghé') || wardName.includes('Bến Thành') || 
              wardName.includes('Hàng Bạc') || wardName.includes('Hàng Gai') ||
              wardName.includes('Thạch Thang') || wardName.includes('Cái Khế') ||
              wardName.includes('Phường 01') || wardName.includes('Phường 1')) {
            wardFee = 0 // Trung tâm tuyệt đối (0₫)
          } 
          // Phường nội thành - tính theo khoảng cách
          else {
            wardFee = Math.round(1000 + distanceRatio * 3000) // 1k-4k
          }
        }
        // THỊ TRẤN (trung bình)
        else if (wardName.includes('Thị trấn') || wardName.includes('TT ')) {
          wardFee = Math.round(2000 + distanceRatio * 3000) // 2k-5k
        }
        // XÃ NGOẠI THÀNH (xa nhất)
        else if (wardName.includes('Xã')) {
          // Xã vùng núi/biên giới (từ khóa đặc biệt)
          if (wardName.includes('Sơn') || wardName.includes('Thượng') || 
              wardName.includes('Cao') || wardName.includes('Núi') ||
              wardName.includes('Rừng') || wardName.includes('Biên') ||
              wardName.includes('Lầu') || wardName.includes('Sử')) {
            wardFee = Math.round(5000 + distanceRatio * 5000) // 5k-10k (vùng núi)
          } 
          // Xã thường - tính theo khoảng cách
          else {
            wardFee = Math.round(3000 + distanceRatio * 4000) // 3k-7k
          }
        }
        // Default
        else {
          wardFee = Math.round(2000 + distanceRatio * 3000) // 2k-5k
        }
        
        // Làm tròn phí về bội số của 500₫ (đẹp hơn)
        wardFee = Math.round(wardFee / 500) * 500
        
        // 4. Phí theo trọng lượng
        const weightFee = Math.ceil(totalWeight / 1000) * 5000 // 5k mỗi kg
        
        // 5. Phí bảo hiểm
        const insuranceFee = insuranceValue > 3000000 ? Math.ceil(insuranceValue * 0.005) : 0
        
        // 🎯 TỔNG PHÍ = Base + Province + District + Ward + Weight + Insurance
        const totalFee = baseFee + provinceFee + districtFee + wardFee + weightFee + insuranceFee
        
        shippingFee.value = totalFee
        
        // Thời gian giao hàng phụ thuộc vào khoảng cách - TỰ ĐỘNG
        let daysToDeliver = 2 // Default
        
        if (bigCities.includes(selectedProvince.value)) {
          // Thành phố lớn: 1-2 ngày
          daysToDeliver = selectedProvince.value === 202 ? 1 : 2
        } else if ([269, 271, 273, 275, 277, 279, 281, 283, 285, 287, 289, 291, 293, 295, 297, 299, 301, 303, 305, 307, 380, 382, 384, 386].includes(selectedProvince.value)) {
          // Miền Bắc: 2-3 ngày
          daysToDeliver = 3
        } else if ([335, 337, 339, 341, 343].includes(selectedProvince.value)) {
          // Tây Nguyên: 3-4 ngày (xa & núi)
          daysToDeliver = 4
        } else if ([309, 311, 313, 315, 317, 319, 321, 323, 325, 327, 329, 331, 333].includes(selectedProvince.value)) {
          // Miền Trung: 3 ngày
          daysToDeliver = 3
        } else {
          // Miền Nam & khác: 2 ngày
          daysToDeliver = 2
        }
        
        expectedDeliveryTime.value = new Date(Date.now() + daysToDeliver * 24 * 60 * 60 * 1000).toISOString()
        
        console.log('✅ [MOCK] Calculated shipping fee:', {
          province: selectedProvince.value,
          district: selectedDistrict.value,
          ward: selectedWard.value,
          wardName: wardName,
          wardPosition: `${wardIndex + 1}/${totalWards}`, // Vị trí xã trong danh sách
          distanceRatio: `${Math.round(distanceRatio * 100)}%`, // % khoảng cách
          totalWeight,
          insuranceValue,
          breakdown: {
            baseFee,
            provinceFee,
            districtFee,
            wardFee, // 🆕 Phí xã theo độ xa (dynamic)
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

  // 🎯 AUTO-CALCULATE: Tự động tính phí ship khi chọn đủ địa chỉ
  watch([selectedProvince, selectedDistrict, selectedWard], ([province, district, ward]) => {
    // Chỉ tự động tính khi đã chọn đủ tỉnh + quận + xã
    if (province && district && ward) {
      console.log('🔄 Auto-calculating shipping fee for:', { province, district, ward })
      // Tính với trọng lượng mặc định 500g và không bảo hiểm
      calculateShippingFee(500, 0)
    } else {
      // Reset phí ship nếu chưa chọn đủ
      shippingFee.value = 0
      expectedDeliveryTime.value = null
    }
  })

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
