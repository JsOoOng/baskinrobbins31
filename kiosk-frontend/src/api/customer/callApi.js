import axios from '@/api/axios';

export const callStaff = async (callRequest) => {
  try {
    const response = await axios.post('/api/calls', callRequest);
    return response.data;
  } catch (error) {
    console.error('직원 호출 API 에러:', error);
    throw error;
  }
};
