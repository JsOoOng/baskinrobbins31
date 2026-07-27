/**
 * [모듈 흐름 안내] headFlavorApi
 * 역할: 아이스크림 맛 화면에서 사용하는 HTTP 요청을 한곳에 모은 API 모듈이다.
 * 호출 흐름: Vue 화면 -> 이 모듈 -> Axios/Vite proxy -> Spring Controller (/head/flavors, /head/flavors/active, /head/flavors/${flavorId})
 * 데이터 기준: 실제 요청 URL과 현재 백엔드 DTO 필드를 우선한다.
 */
import api from '@/api/axios'

/*
 * API 응답에서 실제 데이터 추출
 *
 * 지원 형태:
 * 배열·객체 직접 반환
 * { data: ... }
 * { result: ... }
 */
export const extractFlavorData = (
  responseBody
) => {
  if (
    responseBody === null ||
    responseBody === undefined
  ) {
    return null
  }

  /*
   * Axios Response가 직접 들어온 경우
   */
  if (
    responseBody.data !== undefined
  ) {
    const body =
      responseBody.data

    /*
     * 공통 응답:
     * { data: 실제 데이터 }
     */
    if (
      body &&
      body.data !== undefined
    ) {
      return body.data
    }

    /*
     * 공통 응답:
     * { result: 실제 데이터 }
     */
    if (
      body &&
      body.result !== undefined
    ) {
      return body.result
    }

    return body
  }

  /*
   * 이미 response.data만 전달된 경우
   */
  if (
    responseBody.result !== undefined
  ) {
    return responseBody.result
  }

  return responseBody
}

/*
 * 아이스크림 맛 전체 목록 조회
 *
 * GET /head/flavors
 */
export const getHeadFlavors = async () => {
  const response =
    await api.get(
      '/head/flavors'
    )

  return response.data
}

/*
 * 운영 중인 아이스크림 맛 목록 조회
 *
 * GET /head/flavors/active
 */
export const getActiveHeadFlavors =
  async () => {
    const response =
      await api.get(
        '/head/flavors/active'
      )

    return response.data
  }

/*
 * 아이스크림 맛 상세 조회
 *
 * GET /head/flavors/{flavorId}
 */
export const getHeadFlavorDetail =
  async (
    flavorId
  ) => {
    if (
      flavorId === null ||
      flavorId === undefined
    ) {
      throw new Error(
        '맛 번호가 없습니다.'
      )
    }

    const response =
      await api.get(
        `/head/flavors/${flavorId}`
      )

    return response.data
  }

/*
 * 아이스크림 맛 신규 등록
 *
 * POST /head/flavors
 *
 * 요청:
 * {
 *   flavorName:
 *     '블랙 소르베',
 *
 *   imageUrl:
 *     '/images/flavors/black_sorbet.png'
 * }
 */
export const createHeadFlavor =
  async (
    request
  ) => {
    if (!request) {
      throw new Error(
        '맛 등록 요청 정보가 없습니다.'
      )
    }

    const response =
      await api.post(
        '/head/flavors',
        request,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      )

    return response.data
  }

/*
 * 아이스크림 맛 수정
 *
 * PUT /head/flavors/{flavorId}
 */
export const updateHeadFlavor =
  async (
    flavorId,
    request
  ) => {
    if (
      flavorId === null ||
      flavorId === undefined
    ) {
      throw new Error(
        '맛 번호가 없습니다.'
      )
    }

    if (!request) {
      throw new Error(
        '맛 수정 요청 정보가 없습니다.'
      )
    }

    const response =
      await api.put(
        `/head/flavors/${flavorId}`,
        request,
        {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }
      )

    return response.data
  }

/*
 * 아이스크림 맛 비활성화
 *
 * DELETE /head/flavors/{flavorId}
 */
export const deleteHeadFlavor =
  async (
    flavorId
  ) => {
    if (
      flavorId === null ||
      flavorId === undefined
    ) {
      throw new Error(
        '맛 번호가 없습니다.'
      )
    }

    const response =
      await api.delete(
        `/head/flavors/${flavorId}`
      )

    return response.data
  }