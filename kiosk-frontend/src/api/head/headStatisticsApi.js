/**
 * [모듈 흐름 안내] headStatisticsApi
 * 역할: 통계 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/statistics/summary, /head/statistics/sales-trend, /head/statistics/stores, /head/statistics/products, /head/statistics/flavors)
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '../axios'

const createParams = ({
  startDate,
  endDate,
  storeId,
  period,
  limit
}) => {
  const params = {
    startDate,
    endDate
  }

  if (
    storeId !== '' &&
    storeId !== null &&
    storeId !== undefined
  ) {
    params.storeId = storeId
  }

  if (period) {
    params.period = period
  }

  if (limit) {
    params.limit = limit
  }

  return params
}

/*
 * 요약 통계
 */
export const getHeadStatisticsSummary = async (
  filters
) => {
  const response = await api.get(
    '/head/statistics/summary',
    {
      params: createParams(filters)
    }
  )

  return response.data
}

/*
 * 매출 추이
 */
export const getHeadSalesTrend = async (
  filters
) => {
  const response = await api.get(
    '/head/statistics/sales-trend',
    {
      params: createParams(filters)
    }
  )

  return response.data
}

/*
 * 지점 매출 순위
 */
export const getHeadStoreSalesRanking = async (
  filters
) => {
  const response = await api.get(
    '/head/statistics/stores',
    {
      params: createParams(filters)
    }
  )

  return response.data
}

/*
 * 상품 판매 순위
 */
export const getHeadProductSalesRanking = async (
  filters
) => {
  const response = await api.get(
    '/head/statistics/products',
    {
      params: createParams(filters)
    }
  )

  return response.data
}

/*
 * 맛별 판매 순위
 */
export const getHeadFlavorSalesRanking = async (
  filters
) => {
  const response = await api.get(
    '/head/statistics/flavors',
    {
      params: createParams(filters)
    }
  )

  return response.data
}

/*
 * API 응답 데이터 추출
 */
export const extractStatisticsData = (
  responseBody
) => {
  if (
    responseBody !== null &&
    typeof responseBody === 'object' &&
    Object.prototype.hasOwnProperty.call(
      responseBody,
      'data'
    )
  ) {
    return responseBody.data
  }

  return responseBody
}

/*
 * API 오류 메시지 추출
 */
export const extractStatisticsErrorMessage = (
  error,
  defaultMessage =
    '통계 정보를 불러오지 못했습니다.'
) => {
  const responseData =
    error?.response?.data

  if (typeof responseData === 'string') {
    return responseData
  }

  return (
    responseData?.message ||
    responseData?.error ||
    error?.message ||
    defaultMessage
  )
}