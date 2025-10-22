<script setup>
import { toRefs } from 'vue'

const emit = defineEmits([
  'edit','add-child','delete','drag-start','drag-end','drop','toggle'
])

const props = defineProps({
  node: { type: Object, required: true },
  dragMode: { type: Boolean, default: false }
})

const { node, dragMode } = toRefs(props)

const onToggle    = () => emit('toggle', node.value)
const onEdit      = () => emit('edit', node.value)
const onAdd       = () => emit('add-child', node.value)
const onDelete    = () => emit('delete', node.value)
const onDragStart = () => emit('drag-start', node.value)
const onDragEnd   = () => emit('drag-end')
const onDrop      = (e) => emit('drop', e)
</script>

<template>
  <div
    class="tree-node"
    :class="{ 'has-children': node.children && node.children.length, 'root-node': !node.parentId }"
    :draggable="dragMode"
    @dragstart="onDragStart"
    @dragend="onDragEnd"
    @dragover.prevent
    @drop="onDrop"
  >
    <!-- Header -->
    <div
      class="node-content"
      :class="{ expanded: node.expanded }"
      role="button"
      tabindex="0"
      @click="onToggle"
      @keydown.enter.space.prevent="onToggle"
    >
      <button
        class="expand-btn"
        v-if="node.children && node.children.length"
        @click.stop="onToggle"
        :aria-expanded="!!node.expanded"
        :title="node.expanded ? 'Thu gọn' : 'Mở rộng'"
      >
        <i :class="node.expanded ? 'bi bi-caret-down-fill' : 'bi bi-caret-right-fill'"></i>
      </button>

      <div class="node-info">
        <div class="node-icon">
          <i :class="node.icon || 'bi bi-folder'"></i>
        </div>
        <div class="node-details">
          <div class="node-topline">
            <span class="node-name">{{ node.name }}</span>
            <!-- Số SẢN PHẨM: dùng productCount từ BE -->
            <span class="pill pill-count" :title="(node.productCount || 0) + ' sản phẩm'">
              <i class="bi bi-box"></i> {{ node.productCount || 0 }}
            </span>
          </div>
          <div class="node-meta">
            <i class="bi bi-diagram-3"></i>
            <span>Id: {{ node.id }}</span>
            <span v-if="node.parentId" class="dot">•</span>
            <span v-if="node.parentId">Con của #{{ node.parentId }}</span>
          </div>
        </div>
      </div>

      <div class="node-actions">
        <button class="btn btn-sm btn-outline-primary" @click.stop="onEdit" title="Chỉnh sửa">
          <i class="bi bi-pencil"></i>
        </button>
        <button class="btn btn-sm btn-outline-success" @click.stop="onAdd" title="Thêm danh mục con">
          <i class="bi bi-plus"></i>
        </button>
        <button class="btn btn-sm btn-outline-danger" @click.stop="onDelete" title="Xóa">
          <i class="bi bi-trash"></i>
        </button>
      </div>
    </div>

    <!-- Children nằm TRONG khối cha -->
    <transition name="branch">
      <div
        v-if="node.expanded && node.children && node.children.length"
        class="node-children"
      >
        <TreeNode
          v-for="child in node.children"
          :key="child.id"
          :node="child"
          :dragMode="dragMode"
          @edit="$emit('edit',$event)"
          @add-child="$emit('add-child',$event)"
          @delete="$emit('delete',$event)"
          @drag-start="$emit('drag-start',$event)"
          @drag-end="$emit('drag-end')"
          @drop="$emit('drop',$event)"
          @toggle="$emit('toggle',$event)"
        />
      </div>
    </transition>
  </div>
</template>

<style scoped>
/* ====== Layout cơ bản ====== */
.tree-node {
  border: 1px solid #e9ecef;
  border-radius: 12px;
  background: #fff;
  transition: border-color .18s ease, box-shadow .18s ease, transform .18s ease;
}

.tree-node:hover {
  border-color: #6366f1;
  box-shadow: 0 4px 18px rgba(99,102,241,.08);
}

.node-content {
  display: flex;
  align-items: center;
  gap: .75rem;
  padding: 1rem;
  cursor: pointer;
  border-radius: 12px;
}

.node-content:focus-visible {
  outline: 2px solid #a5b4fc;
  outline-offset: 2px;
}

/* ====== Nút mở rộng ====== */
.expand-btn {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  background: #fff;
  color: #6b7280;
  transition: all .18s ease;
  flex-shrink: 0;
}
.expand-btn:hover {
  background: #f6f7fb;
  color: #4f46e5;
  border-color: #dfe3ea;
}

/* ====== Icon + text ====== */
.node-info {
  display: flex;
  align-items: center;
  gap: .75rem;
  flex: 1;
  min-width: 0;
}

.node-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(180deg, #f7f7ff, #f1f4ff);
  color: #6366f1;
  display: grid;
  place-items: center;
  font-size: 1.1rem;
  box-shadow: inset 0 0 0 1px #e9ecff;
  flex-shrink: 0;
}

.node-details {
  flex: 1;
  min-width: 0;
}

.node-topline {
  display: flex;
  align-items: center;
  gap: .5rem;
}

.node-name {
  font-weight: 700;
  color: #1f2937;
  letter-spacing: .2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: .35rem;
  font-size: .75rem;
  border-radius: 999px;
  padding: .15rem .5rem;
  line-height: 1;
  white-space: nowrap;
}
.pill-count {
  background: #eef2ff;
  color: #4338ca;
  border: 1px solid #e0e7ff;
}

.node-meta {
  margin-top: .2rem;
  font-size: .83rem;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: .4rem;
}
.node-meta .dot { opacity: .6; }

/* ====== Actions ====== */
.node-actions {
  display: flex;
  gap: .4rem;
}
.node-actions .btn {
  padding: .35rem .55rem;
  border-radius: 10px;
}

/* ====== Children (cha ôm con) ====== */
.node-children {
  border-top: 1px solid #edf0f3;
  background: #fbfbfe;
  padding: .6rem .6rem .8rem .9rem;
  position: relative;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

/* Connector dọc */
.node-children::before {
  content: "";
  position: absolute;
  top: .4rem;
  bottom: .6rem;
  left: .55rem;
  width: 2px;
  background: #e5e7eb;
  border-radius: 2px;
}

/* Child items gọn gàng, không “hộp lồng hộp” quá mạnh */
.node-children .tree-node {
  border: none;
  box-shadow: none;
  background: transparent;
  margin: .25rem 0 0 .5rem;
}
.node-children .tree-node > .node-content {
  padding: .65rem .75rem;
  border: 1px solid #eef1f4;
  border-radius: 10px;
  background: #fff;
  transition: border-color .15s ease, box-shadow .15s ease;
}
.node-children .tree-node > .node-content:hover {
  border-color: #cad2ff;
  box-shadow: 0 2px 10px rgba(99,102,241,.06);
}

/* Root nhấn mạnh nhẹ */
.root-node > .node-content {
  padding: 1.05rem 1rem;
  background: linear-gradient(90deg, #ffffff, #fbfbfe);
  border-left: 4px solid rgba(99,102,241,.16);
}

/* ====== Transition mở/đóng nhánh ====== */
.branch-enter-from,
.branch-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
.branch-enter-active,
.branch-leave-active {
  transition: all .18s ease;
}
</style>
