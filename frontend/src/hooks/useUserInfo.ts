import { useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../store";
import { setUser, setLoading, setError, clearUser } from "../store/slices/userSlice";
import { useApi } from "./useApi";
import { User } from "../types/Auth/User";

export const useUserInfo = (autoFetch: boolean = true) => {
  const dispatch = useDispatch();
  const { user, loading, error } = useSelector((state: RootState) => state.user);
  const { execute: fetchUserInfoApi } = useApi<User>("/api/member/myinfo", "GET");

  const fetchUserInfo = useCallback(async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      console.log("토큰이 없어서 사용자 정보 요청 중단");
      dispatch(clearUser());
      return;
    }

    try {
      console.log("사용자 정보 요청 시작");
      dispatch(setLoading(true));
      dispatch(setError(null));

      // 사용자 정보 조회
      const response = await fetchUserInfoApi();
      console.log("사용자 정보 응답:", response);

      if (response.isSuccess && response.result) {
        console.log("사용자 정보 설정:", response.result);
        dispatch(setUser(response.result));
      } else {
        console.error("사용자 정보 요청 실패:", response.error);
        dispatch(setError(response.error));
        dispatch(clearUser());
      }
    } catch (err: any) {
      console.error("사용자 정보 요청 오류:", err);
      dispatch(setError(err.message || "사용자 정보를 가져오는데 실패했습니다."));
      dispatch(clearUser());
      if (err.status === 401) {
        localStorage.removeItem("accessToken");
      }
    } finally {
      console.log("사용자 정보 요청 완료");
      dispatch(setLoading(false));
    }
  }, [dispatch, fetchUserInfoApi]);

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    console.log("useUserInfo 초기화 - 토큰 존재:", !!token, "autoFetch:", autoFetch);

    if (autoFetch && token && !user) {
      console.log("사용자 정보 자동 요청 시작");
      fetchUserInfo();
    } else if (!token) {
      console.log("토큰이 없어서 사용자 정보 초기화");
      dispatch(clearUser());
    }
  }, [autoFetch, fetchUserInfo, user]);

  // localStorage의 accessToken 변경 감지
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      console.log("스토리지 변경 감지:", e.key);
      if (e.key === "accessToken") {
        if (e.newValue) {
          console.log("새 토큰 감지, 사용자 정보 갱신");
          fetchUserInfo();
        } else {
          console.log("토큰 제거 감지, 사용자 정보 초기화");
          dispatch(clearUser());
        }
      }
    };

    window.addEventListener("storage", handleStorageChange);
    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, [fetchUserInfo, dispatch]);

  const clearUserInfo = useCallback(() => {
    console.log("사용자 정보 초기화");
    dispatch(clearUser());
  }, [dispatch]);

  return {
    user,
    loading,
    error,
    fetchUserInfo,
    clearUserInfo,
  };
};
