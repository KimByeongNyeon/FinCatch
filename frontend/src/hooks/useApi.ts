import { useCallback, useState } from "react";
import axiosInstance from "../api/axios";
import { Response } from "../types/response/Response";

interface CustomConfig {
  url?: string;
  headers?: Record<string, string>;
  params?: Record<string, any>;
}

export const useApi = <T, P = void>(endpoint: string, method: "GET" | "PATCH" | "POST" | "DELETE" = "GET") => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<T | null>(null);

  const execute = useCallback(
    async (payload?: P, config?: CustomConfig) => {
      setLoading(true);
      setError(null);

      try {
        let response;
        const url = config?.url || endpoint;
        const headers = config?.headers || {};
        const params = config?.params;

        switch (method) {
          case "GET":
            response = await axiosInstance.get<Response<T>>(url, { headers });
            break;
          case "PATCH":
            response = await axiosInstance.patch<Response<T>>(url, payload, { headers, params });
            break;
          case "DELETE":
            response = await axiosInstance.delete<Response<T>>(url, {
              headers,
              data: payload,
            });
            break;

          case "POST":
            response = await axiosInstance.post<Response<T>>(url, payload, {
              headers,
            });
            break;
        }

        const responseData = response.data;

        // 기존 API 응답 처리
        if (responseData.isSuccess) {
          setData(responseData.result);
          return responseData;
        } else {
          setError(responseData.message);
          return {
            isSuccess: false,
            code: responseData.code || 500,
            message: responseData.message,
            result: null,
          };
        }
      } catch (err: any) {
        // 401 에러 (인증 실패) 처리

        // 기타 오류 처리
        const errorMessage = err.response?.data?.message || err.message || "알 수 없는 오류 발생";
        setError(errorMessage);
        return { success: false, error: errorMessage };
      } finally {
        setLoading(false);
      }
    },
    [endpoint, method]
  );

  return { data, loading, error, execute, setData };
};
