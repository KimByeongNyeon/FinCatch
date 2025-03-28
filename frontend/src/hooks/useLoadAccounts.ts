import { useState, useCallback } from "react";
import { Account } from "../types/Accounts/Account";
import { useAccount } from "./useAccount";

interface UseLoadAccountsReturn {
  accounts: Account[];
  loading: boolean;
  error: string | null;
  loadAccounts: () => Promise<void>;
}

// 더미 데이터

export const useLoadAccounts = (): UseLoadAccountsReturn => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const { fetchAllAccount } = useAccount();

  const loadAccounts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // 실제 API 호출 대신 더미 데이터 사용
      // await new Promise((resolve) => setTimeout(resolve, 700)); // 로딩 시뮬레이션
      // setAccounts(dummyAccounts);

      // 실제 API 호출 코드 (주석 처리)
      const response = await fetchAllAccount();
      if (response.isSuccess && response.result) {
        setAccounts(response.result.accounts);
      } else {
        setError("계좌 정보를 불러오는데 실패했습니다.");
      }
    } catch (error) {
      setError("계좌 정보를 불러오는데 실패했습니다.");
    }
    setLoading(false);
  }, []);

  return {
    accounts,
    loading,
    error,
    loadAccounts,
  };
};
