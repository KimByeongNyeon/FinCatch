export interface Account {
  accountNo: string;
  bankCode: string;
  accountName: string;
  accountBalance: number;
}

export interface AllAccount {
  mainAccount: string;
  accounts: Account[];
}

export interface AccountDetail {
  bankCode: string;
  bankName: string;
  userName: string;
  accountNo: string;
  accountName: string;
  accountTypeCode: string;
  accountTypeName: string;
  accountCreateDate: string;
  accountExpiryDate: string;
  dailyTransferLimit: number;
  oneTimeTransferLimit: number;
  accountBalance: number;
  lastTranscationDate: string;
  currency: string;
}

export interface ConsumeHistory {
  transactionUniqueNo: number;
  transactionDate: string;
  transactionTime: string;
  transactionType: string;
  transactionTypeName: string;
  transactionAccountNo: string | null;
  transactionBalance: number;
  transactionAfterBalance: number;
  transactionSummary: string;
  transactionMemo: string | null;
}

export interface ConsumeHistoryREC {
  totalCount: string;
  list: ConsumeHistory[];
}

export interface ConsumeHistoryHeader {
  responseCode: string;
  responseMessage: string;
  apiName: string;
  transmissionDate: string;
  transmissionTime: string;
  institutionCode: string;
  apiKey: string;
  apiServiceCode: string;
  institutionTransactionUniqueNo: string;
}

export interface ConsumeHistoryResponse {
  Header: ConsumeHistoryHeader;
  REC: ConsumeHistoryREC;
}

export interface AllConsumeHistory {
  isSuccess: boolean;
  code: number;
  message: string;
  result: ConsumeHistoryResponse;
}
