import React, { useState, useCallback, useEffect } from "react";
import { bankLogo } from "../../utils/BankLogo";
import { AccountDetail, AllConsumeHistory, ConsumeHistory } from "../../types/Accounts/Account";
import { formatDateToInput } from "../../utils/formatDate";
import { formatBalance, formataccountNo } from "../../utils/formatAccount";
import { useAccount } from "../../hooks/useAccount";

interface MainAccount {
  bankCode: string;
  accountNo: string;
  accountName: string;
  accountBalance: number;
}

interface AccountLinkSectionProps {
  onAccountLink: () => void;
  mainAccount: MainAccount | null;
  error?: string | null;
}

const AccountLinkSection: React.FC<AccountLinkSectionProps> = ({ onAccountLink, mainAccount, error }) => {
  const [showDetail, setShowDetail] = useState(false);
  const [accountDetail, setAccountDetail] = useState<AccountDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [consumeHistory, setConsumeHistory] = useState<AllConsumeHistory | null>(null);
  const { fetchAccountDetail, fetchConsumeHistory, patchAccount } = useAccount();

  // 거래내역 필터 상태
  const [historyFilter, setHistoryFilter] = useState({
    startDate: new Date(new Date().setMonth(new Date().getMonth() - 1)).toISOString().split("T")[0].replace(/-/g, ""),
    endDate: new Date().toISOString().split("T")[0].replace(/-/g, ""),
    transactionType: "A" as "A" | "M" | "D",
  });

  // 상세정보 조회
  const handleDetailClick = async () => {
    const newShowDetail = !showDetail;
    setShowDetail(newShowDetail);

    if (newShowDetail && mainAccount) {
      setLoading(true);
      try {
        const response = await fetchAccountDetail(mainAccount.accountNo);
        if (response?.isSuccess && response.result) {
          setAccountDetail(response.result);
        }
      } catch (error) {
        console.error("계좌 상세 조회 에러:", error);
      } finally {
        setLoading(false);
      }
    }
  };

  // 거래내역 조회
  const fetchHistory = async () => {
    if (!mainAccount) return;

    setHistoryLoading(true);
    try {
      const response = await fetchConsumeHistory(mainAccount.accountNo, historyFilter.startDate, historyFilter.endDate, historyFilter.transactionType);
      console.log("거래내역 응답 (AccountLinkSection):", JSON.stringify(response, null, 2));

      if (response?.isSuccess && response.result) {
        console.log("거래내역 데이터 구조:", {
          Header: response.result.Header,
          REC: response.result.REC,
          rawResponse: response,
        });

        // 응답 구조 검증
        if (!response.result.REC?.list) {
          console.error("거래내역 데이터 구조가 올바르지 않습니다:", response.result);
          return;
        }

        // 거래내역 데이터 출력
        console.log("거래내역 목록:", response.result.REC.list);
        console.log("거래내역 총 개수:", response.result.REC.totalCount);

        setConsumeHistory(response);
      } else {
        console.error("거래내역 조회 실패:", response?.message);
      }
    } catch (error) {
      console.error("거래내역 조회 에러:", error);
    } finally {
      setHistoryLoading(false);
    }
  };

  // 최초 마운트 시 거래내역 조회
  useEffect(() => {
    if (mainAccount) {
      fetchHistory();
    }
  }, [mainAccount?.accountNo]); // 계좌가 변경될 때만 실행

  const handleFilterChange = (name: string, value: string) => {
    if (name === "startDate" || name === "endDate") {
      value = value.replace(/-/g, "");
    }
    setHistoryFilter((prev) => {
      const newFilter = {
        ...prev,
        [name]: value,
      };
      // 필터 업데이트 후 즉시 조회
      setTimeout(() => {
        fetchHistory();
      }, 0);
      return newFilter;
    });
  };

  const handleAccountLinkClick = async () => {
    try {
      setLoading(true);
      await patchAccount();
      onAccountLink();
    } catch (error) {
      console.error("계좌 목록 갱신 중 오류 발생:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white/95 rounded-2xl shadow-2xl p-6 mt-6">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-xl font-bold text-gray-800 font-korean-pixel">💰 주 거래 통장</h3>
        {mainAccount && (
          <div className="flex gap-2">
            <button onClick={handleDetailClick} className="px-4 py-2 bg-blue-50 text-blue-600 rounded-lg font-korean-pixel hover:bg-blue-100 transition-colors">
              {showDetail ? "상세정보 닫기" : "상세정보 보기"}
            </button>
            <button onClick={handleAccountLinkClick} className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-korean-pixel hover:bg-gray-200 transition-colors">
              계좌 변경
            </button>
          </div>
        )}
      </div>

      {mainAccount ? (
        <>
          <div className="bg-gray-50 rounded-xl p-6">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div className="w-12 h-12 flex items-center justify-center">
                  <img src={bankLogo[mainAccount.bankCode.toString()]} alt="bank logo" className="max-w-full max-h-full" />
                </div>
                <div>
                  <div className="text-lg font-korean-pixel text-gray-800">{mainAccount.accountName}</div>
                  <div className="text-sm font-korean-pixel text-gray-600">{mainAccount.accountNo}</div>
                </div>
              </div>
              <div className="text-right">
                <div className="text-lg font-semibold font-korean-pixel text-gray-800">{formatBalance(mainAccount.accountBalance)}원</div>
              </div>
            </div>
          </div>

          {showDetail && (
            <div className="mt-6 bg-white rounded-xl border border-gray-100 p-6">
              {loading ? (
                <div className="flex justify-center items-center h-40">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-700"></div>
                </div>
              ) : accountDetail ? (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">은행명</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{accountDetail.bankName}</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">예금주</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{accountDetail.userName}</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">계좌 종류</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{accountDetail.accountTypeName}</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">개설일</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{formatDateToInput(accountDetail.accountCreateDate)}</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">1회 이체한도</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{formatBalance(accountDetail.oneTimeTransferLimit)}원</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">1일 이체한도</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{formatBalance(accountDetail.dailyTransferLimit)}원</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">최근 거래일</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{formatDateToInput(accountDetail.lastTranscationDate)}</p>
                    </div>
                    <div className="space-y-2">
                      <p className="text-sm text-gray-500 font-korean-pixel">통화</p>
                      <p className="text-base text-gray-800 font-korean-pixel">{accountDetail.currency}</p>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="text-center text-gray-500 font-korean-pixel">계좌 상세 정보를 불러올 수 없습니다.</div>
              )}
            </div>
          )}

          {/* 거래내역 필터 및 목록 */}
          <div className="mt-6 bg-white rounded-xl border border-gray-100 p-6">
            <h4 className="text-lg font-bold text-gray-800 font-korean-pixel mb-4">💸 거래내역</h4>

            <div className="flex gap-4 mb-6">
              <div className="flex-1">
                <label className="block text-sm text-gray-500 font-korean-pixel mb-1">조회 시작일</label>
                <input
                  type="date"
                  value={formatDateToInput(historyFilter.startDate)}
                  onChange={(e) => handleFilterChange("startDate", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-200 rounded-lg font-korean-pixel"
                />
              </div>
              <div className="flex-1">
                <label className="block text-sm text-gray-500 font-korean-pixel mb-1">조회 종료일</label>
                <input
                  type="date"
                  value={formatDateToInput(historyFilter.endDate)}
                  onChange={(e) => handleFilterChange("endDate", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-200 rounded-lg font-korean-pixel"
                />
              </div>
              <div className="flex-1">
                <label className="block text-sm text-gray-500 font-korean-pixel mb-1">거래 유형</label>
                <select
                  value={historyFilter.transactionType}
                  onChange={(e) => handleFilterChange("transactionType", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-200 rounded-lg font-korean-pixel"
                >
                  <option value="A">전체</option>
                  <option value="M">입금</option>
                  <option value="D">출금</option>
                </select>
              </div>
            </div>

            {historyLoading ? (
              <div className="flex justify-center items-center h-40">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-700"></div>
              </div>
            ) : consumeHistory?.result?.REC?.list && consumeHistory.result.REC.list.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-korean-pixel text-gray-500">거래일시</th>
                      <th className="px-6 py-3 text-left text-xs font-korean-pixel text-gray-500">거래내용</th>
                      <th className="px-6 py-3 text-right text-xs font-korean-pixel text-gray-500">거래금액</th>
                      <th className="px-6 py-3 text-right text-xs font-korean-pixel text-gray-500">잔액</th>
                    </tr>
                  </thead>
                  <tbody>
                    {consumeHistory.result.REC.list.map((transaction: ConsumeHistory) => (
                      <tr key={transaction.transactionUniqueNo} className="border-b border-gray-100">
                        <td className="px-6 py-4 text-sm font-korean-pixel text-gray-500">
                          {`${transaction.transactionDate.replace(/(\d{4})(\d{2})(\d{2})/, "$1-$2-$3")} ${transaction.transactionTime.replace(/(\d{2})(\d{2})(\d{2})/, "$1:$2:$3")}`}
                        </td>
                        <td className="px-6 py-4 text-sm font-korean-pixel">{transaction.transactionSummary}</td>
                        <td className={`px-6 py-4 text-sm font-korean-pixel text-right ${transaction.transactionTypeName === "출금" ? "text-red-600" : "text-blue-600"}`}>
                          {transaction.transactionTypeName === "출금" ? "-" : "+"}
                          {formatBalance(transaction.transactionBalance)}원
                        </td>
                        <td className="px-6 py-4 text-sm font-korean-pixel text-right">{formatBalance(transaction.transactionAfterBalance)}원</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="text-center py-10 text-gray-500 font-korean-pixel">조회된 거래 내역이 없습니다.</div>
            )}
          </div>
        </>
      ) : (
        <div className="bg-gray-50 rounded-xl p-6 text-center">
          <p className="text-gray-600 mb-4 font-korean-pixel">{error || "아직 주 거래 통장이 설정되지 않았습니다"}</p>
          <button
            onClick={handleAccountLinkClick}
            className="px-6 py-3 bg-gradient-to-r from-form-color to-button-color text-gray-700 rounded-lg font-korean-pixel hover:opacity-90 transition-all duration-300"
          >
            주 거래 통장 설정하기
          </button>
        </div>
      )}
    </div>
  );
};

export default AccountLinkSection;
