<script setup>
import { ref, onMounted } from 'vue'
import SanPhamService from '@/services/sanPhamService'

const danhSach = ref([])

const laySanPham = async () => {
  try {
    const res = await SanPhamService.layDanhSach()
    danhSach.value = res.data
  } catch (err) {
    console.error('Lỗi khi tải sản phẩm:', err)
  }
}

onMounted(() => laySanPham())

const themSanPham = async () => {
  const payload = {
    ten: 'SP Test',
    slug: 'sp-test',
    thuongHieu: 'BrandX',
    moTa: 'Mô tả',
    huongDanBaoQuan: 'Bảo quản',
    bangSizeJson: '{}',
    danhMuc: 'A',
    trangThai: 1,
  }

  try {
    const res = await SanPhamService.themSanPham(payload)
    console.log('Thêm thành công:', res.data)
    laySanPham() // refresh danh sách
  } catch (err) {
    console.error('Lỗi khi thêm:', err)
  }
}
</script>

<template>
  <div class="container mt-4">
    <h3>Quản lý sản phẩm</h3>

    <button class="btn btn-primary my-3" @click="themSanPham">Thêm sản phẩm test</button>

    <table class="table table-striped">
      <thead>
        <tr>
          <th>Tên sản phẩm</th>
          <th>Thương hiệu</th>
          <th>Trạng thái</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="sp in danhSach" :key="sp.id">
          <td>{{ sp.ten }}</td>
          <td>{{ sp.thuongHieu }}</td>
          <td>{{ sp.trangThai === 1 ? 'Hoạt động' : 'Ẩn' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
