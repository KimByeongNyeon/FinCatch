import { useApi } from "./useApi";
import { User } from "../types/Auth/User";

export const useChangeCat = () => {
  const { loading, error, execute: changeCatApi } = useApi<User>("/api/member/maincat", "PATCH");

  const changeCat = async (catId: number) => {
    const response = await changeCatApi(undefined, {
      params: { catId },
    });

    return {
      success: response.isSuccess,
      data: response.result,
      error: response.message,
    };
  };

  return {
    changeCat,
    loading,
    error,
  };
};
