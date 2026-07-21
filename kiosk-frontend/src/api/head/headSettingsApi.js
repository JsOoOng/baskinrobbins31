import axios from 'axios';

const api = axios.create({
  baseURL: '/proxy-api/head/settings',
  timeout: 5000
});

export const getHeadSettings = async () => {
  const response = await api.get('');
  return response.data;
};

export const updateHeadSettings = async (settingsDto) => {
  const response = await api.put('', settingsDto);
  return response.data;
};
