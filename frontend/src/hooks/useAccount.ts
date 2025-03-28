import { useApi } from "./useApi";
import { AllAccount, AccountDetail, AllConsumeHistory, ConsumeHistoryResponse, ConsumeHistoryHeader, ConsumeHistoryREC, Account } from "../types/Accounts/Account";
import { useCallback, useState, useEffect } from "react";
import { useUserInfo } from "./useUserInfo";

export const useAccount = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [error, setError] = useState<string | null>(null);
  const allAccountApi = useApi<AllAccount>("/api/finance/account/all", "POST");
  const patchAccountApi = useApi<AllAccount>("/api/finance/account/all", "PATCH");
  const accountDetailApi = useApi<AccountDetail, { accountNo: string }>("/api/finance/account/detail", "POST");
  const consumeHistoryApi = useApi<ConsumeHistoryResponse, { accountNo: string; startDate: string; endDate: string; transactionType: string }>("/api/finance/account/transactions", "POST");

  const changeAccountApi = useApi<string, { accountNo: string }>("/api/finance/account/change", "PATCH");
  const { user } = useUserInfo(false); // autoFetch를 false로 설정하여 자동 조회 방지

  const fetchAllAccount = useCallback(async () => {
    try {
      console.log("계좌 목록 API 호출 시작");
      const response = await allAccountApi.execute();
      console.log("계좌 목록 API 응답:", response);

      // financeMember가 null인 경우도 정상적으로 처리
      if (response.isSuccess && response.result) {
        setAccounts(response.result.accounts || []);
        setError(null);
      } else {
        setAccounts([]);
        // 에러 메시지 구체화
        const errorMessage = response.message?.includes("financeMember") ? "금융 회원 정보가 없습니다. 계좌를 연동해주세요." : "계좌 정보를 가져오는데 실패했습니다.";
        setError(errorMessage);
      }

      return response;
    } catch (error: any) {
      console.error("계좌 목록 조회 에러:", error);
      setAccounts([]);
      setError("계좌 정보를 가져오는데 실패했습니다.");
      return {
        isSuccess: false,
        code: 500,
        message: error?.message || "계좌 정보를 가져오는데 실패했습니다.",
        result: null,
      };
    }
  }, [allAccountApi]);

  // 사용자 정보가 있을 때 한 번만 계좌 목록 조회
  useEffect(() => {
    if (user) {
      fetchAllAccount().catch((error) => {
        console.error("계좌 목록 조회 실패:", error);
      });
    }
  }, [user]);

  const fetchAccountDetail = useCallback(
    async (accountNo: string) => {
      try {
        const response = await accountDetailApi.execute({ accountNo });
        return response;
      } catch (error) {
        console.error("계좌 상세 조회 에러:", error);
        throw error;
      }
    },
    [accountDetailApi]
  );

  const fetchConsumeHistory = useCallback(
    async (accountNo: string, startDate: string, endDate: string, transactionType: string) => {
      try {
        console.log("거래내역 조회 요청:", { accountNo, startDate, endDate, transactionType });
        const response = await consumeHistoryApi.execute({ accountNo, startDate, endDate, transactionType });
        console.log("거래내역 API 응답 (전체):", JSON.stringify(response, null, 2));

        if (response.isSuccess && response.result) {
          // API 응답이 이미 ConsumeHistoryResponse 형식과 일치하는 경우
          if (response.result.Header && response.result.REC) {
            return {
              isSuccess: true,
              code: 200,
              message: "요청에 성공하였습니다.",
              result: response.result,
            };
          }

          // API 응답을 ConsumeHistoryResponse 형식으로 변환
          const header: ConsumeHistoryHeader = {
            responseCode: "H0000",
            responseMessage: "정상처리 되었습니다.",
            apiName: "inquireTransactionHistoryList",
            transmissionDate: startDate,
            transmissionTime: new Date().toTimeString().slice(0, 6).replace(/:/g, ""),
            institutionCode: "00100",
            apiKey: "d51c7ced1310451093c130348f1167e9",
            apiServiceCode: "inquireTransactionHistoryList",
            institutionTransactionUniqueNo: `${startDate}${new Date().toTimeString().slice(0, 6).replace(/:/g, "")}733173`,
          };

          const rec: ConsumeHistoryREC = {
            totalCount: String(response.result.REC?.totalCount || 0),
            list: Array.isArray(response.result.REC?.list) ? response.result.REC.list : [],
          };

          const transformedResult: ConsumeHistoryResponse = {
            Header: header,
            REC: rec,
          };

          return {
            isSuccess: true,
            code: 200,
            message: "요청에 성공하였습니다.",
            result: transformedResult,
          };
        }

        return {
          isSuccess: false,
          code: 500,
          message: "거래내역 조회에 실패했습니다.",
          result: null,
        };
      } catch (error) {
        console.error("거래 내역 조회 에러:", error);
        throw error;
      }
    },
    [consumeHistoryApi]
  );

  const patchAccount = useCallback(async () => {
    try {
      console.log("계좌 목록 갱신 시작");
      const response = await patchAccountApi.execute();
      if (response?.isSuccess) {
        // 계좌 목록 새로고침
        await fetchAllAccount();
      }
      return response;
    } catch (error) {
      console.error("계좌 목록 갱신 에러:", error);
      throw error;
    }
  }, [patchAccountApi, fetchAllAccount]);

  const changeAccount = useCallback(
    async (accountNo: string) => {
      try {
        const response = await changeAccountApi.execute({ accountNo });
        return response;
      } catch (error) {
        console.error("계좌 변경 에러:", error);
        throw error;
      }
    },
    [changeAccountApi]
  );

  return {
    accounts,
    error,
    fetchAllAccount,
    fetchAccountDetail,
    fetchConsumeHistory,
    changeAccount,
    patchAccount,
  };
};
