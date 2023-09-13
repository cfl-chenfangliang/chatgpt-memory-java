import {postRequest} from "./api";

export const completion = async (userId: string, text: string) => {

  return await postRequest({
    url: '/chat/completions',
    data: {
      userId: userId,
      input: text,
    }
  })
}
