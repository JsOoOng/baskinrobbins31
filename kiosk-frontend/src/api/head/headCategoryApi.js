/**
 * [모듈 흐름 안내] headCategoryApi
 * 역할: 상품 카테고리 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/categories, /head/categories/${categoryId})
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

/*
 * 본사 카테고리 목록 조회
 *
 * GET /head/categories
 */
export const getHeadCategories = async () => {
  const response = await api.get('/head/categories')

  return response.data
}

/*
 * 본사 카테고리 상세 조회
 *
 * GET /head/categories/{categoryId}
 */
export const getHeadCategoryDetail = async (categoryId) => {
  const response = await api.get(
    `/head/categories/${categoryId}`
  )

  return response.data
}

/*
 * 본사 카테고리 등록
 *
 * POST /head/categories
 */
export const createHeadCategory = async (categoryForm) => {
  const response = await api.post(
    '/head/categories',
    {
      categoryName: categoryForm.categoryName,
      displayOrder: categoryForm.displayOrder
    }
  )

  return response.data
}

/*
 * 본사 카테고리 수정
 *
 * PUT /head/categories/{categoryId}
 */
export const updateHeadCategory = async (
  categoryId,
  categoryForm
) => {
  const response = await api.put(
    `/head/categories/${categoryId}`,
    {
      categoryName: categoryForm.categoryName,
      displayOrder: categoryForm.displayOrder
    }
  )

  return response.data
}

/*
 * 본사 카테고리 삭제
 *
 * DELETE /head/categories/{categoryId}
 */
export const deleteHeadCategory = async (categoryId) => {
  const response = await api.delete(
    `/head/categories/${categoryId}`
  )

  return response.data
}

/*
 * 카테고리 API 오류 메시지 추출
 */
export const extractCategoryErrorMessage = (
  error,
  defaultMessage = '카테고리 요청 처리 중 오류가 발생했습니다.'
) => {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    (
      typeof error?.response?.data === 'string'
        ? error.response.data
        : null
    ) ||
    error?.message ||
    defaultMessage
  )
}