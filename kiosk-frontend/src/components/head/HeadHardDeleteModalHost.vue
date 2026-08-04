<script setup>
import HeadHardDeleteModal from './HeadHardDeleteModal.vue'
import {
  hardDeleteDialogState,
  closeHardDeleteDialog,
  confirmHardDeleteDialog,
} from './headHardDeleteDialog'
</script>

<template>
  <!-- 쉬운주석: 본사 안의 모든 삭제 버튼이 함께 사용하는 문구 입력 모달이다. -->
  <HeadHardDeleteModal
    :open="hardDeleteDialogState.open"
    :database-name="hardDeleteDialogState.databaseName"
    :target-label="hardDeleteDialogState.targetLabel"
    :deleting="hardDeleteDialogState.deleting"
    @close="closeHardDeleteDialog"
    @confirm="confirmHardDeleteDialog"
  />
</template>
