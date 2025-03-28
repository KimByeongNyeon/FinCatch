import { useApi } from "./useApi";
import { User } from "../types/Auth/User"; // 경로는 실제 구조에 맞게 조정

/**
 * 닉네임 중복 체크 훅
 */
export const useCheckNickname = () => {
  const { loading, error, execute: checkNickname } = useApi<boolean>("api/member/public/nickname", "GET");

  /**
   * 닉네임 중복 체크 함수
   * @param nickname 체크할 닉네임
   * @returns 결과 객체 (success, data: boolean)
   * data가 true면 이미 사용 중, false면 사용 가능
   */
  const checkNicknameAvailability = async (nickname: string) => {
    return await checkNickname(undefined, {
      url: `api/member/public/nickname`,
    });
  };

  return {
    checkNicknameAvailability,
    loading,
    error,
  };
};

/**
 * 닉네임 변경 훅
 */
export const useUpdateNickname = () => {
  const { loading, error, execute: updateNickname } = useApi<User>("api/member/nickname", "PATCH");

  /**
   * 닉네임 변경 함수
   * @param nickname 새 닉네임
   * @returns 결과 객체 (success, data: User)
   */
  const changeNickname = async (nickname: string) => {
    return await updateNickname(undefined, {
      params: { nickname },
    });
  };

  return {
    changeNickname,
    loading,
    error,
  };
};

/**
 * 닉네임 관련 통합 훅
 * 중복 체크와 변경을 함께 처리
 */
export const useNicknameManager = () => {
  const { checkNicknameAvailability, loading: checkLoading, error: checkError } = useCheckNickname();

  const { changeNickname, loading: updateLoading, error: updateError } = useUpdateNickname();

  /**
   * 닉네임 체크 후 사용 가능하면 변경하는 함수
   * @param nickname 새 닉네임
   * @returns 결과 객체 (success, message 등)
   */
  const checkAndUpdateNickname = async (nickname: string) => {
    // 1. 닉네임 중복 체크
    const checkResult = await checkNicknameAvailability(nickname);
    console.log("닉네임 체크 결과:", checkResult);

    if (!checkResult.isSuccess) {
      return {
        success: false,
        message: checkResult.error || "닉네임 체크 중 오류가 발생했습니다.",
      };
    }

    // API 응답의 result가 true면 닉네임이 이미 사용 중, false면 사용 가능
    const isDuplicated = checkResult.result;

    if (isDuplicated) {
      return {
        success: false,
        message: "이미 사용 중인 닉네임입니다.",
      };
    }

    // 2. 닉네임 변경 (사용 가능한 경우)
    const updateResult = await changeNickname(nickname);

    if (!updateResult.isSuccess) {
      return {
        success: false,
        message: updateResult.error || "닉네임 변경 중 오류가 발생했습니다.",
      };
    }

    return {
      success: true,
      message: "닉네임이 성공적으로 변경되었습니다.",
      user: updateResult.result,
    };
  };

  return {
    checkNicknameAvailability,
    changeNickname,
    checkAndUpdateNickname,
    loading: checkLoading || updateLoading,
    error: checkError || updateError,
  };
};
