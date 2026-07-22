import axios from '../axios';

export const getHeadSettings = async () => {
  const response = await axios.get('/head/settings');
  return response.data;
};

export const updateHeadSettings = async (settingsDto) => {
  const response = await axios.put('/head/settings', settingsDto);
  return response.data;
};
