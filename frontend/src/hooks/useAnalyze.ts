import { useApi } from "./useApi";
import { useCallback, useEffect, useState } from "react";
import { Category } from "../types/analysis/Problem";

interface AnalyzePayload {
  quizId: number;
}

interface AnalyzeResponse {
  analysis: string;
  weakness: string;
  recommendation: string;
}

export interface WrongAnswer {
  quizLogId: number;
  memberId: number;
  quizId: number;
  createdAt: Date;
  userAnswer: string;
  isCorrect: boolean;
}

interface AllWrongAnswer {
  allWrongAnswer: WrongAnswer[];
}

export const useAnalyze = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const { loading, error, execute: analyzeAnswer } = useApi<AnalyzeResponse, AnalyzePayload>("api/ai/analyze", "POST");
  const { execute: readWrongAnswer } = useApi<AllWrongAnswer>("api/quiz/wrong", "GET");

  const analyzeWrongAnswer = useCallback(
    async (quizId: number) => {
      return await analyzeAnswer({
        quizId,
      });
    },
    [analyzeAnswer]
  );

  const readAllWrongAnswer = useCallback(async () => {
    return await readWrongAnswer();
  }, [readWrongAnswer]);

  // 틀린 문제 데이터를 카테고리 형태로 변환하는 함수
  const transformWrongAnswersToCategories = (wrongAnswers: WrongAnswer[]): Category[] => {
    // 문제들을 카테고리별로 그룹화
    const groupedByCategory = wrongAnswers.reduce((acc, wrongAnswer) => {
      // TODO: 실제 카테고리 정보를 가져오는 API 호출 필요
      const categoryId = 1; // 임시 카테고리 ID
      if (!acc[categoryId]) {
        acc[categoryId] = {
          id: categoryId,
          name: "금융통화위원회의 역할", // 임시 카테고리 이름
          problems: [],
          totalProblems: 5,
        };
      }
      acc[categoryId].problems.push({
        id: wrongAnswer.quizId,
        title: `문제 ${wrongAnswer.quizId}`, // TODO: 실제 문제 제목을 가져오는 API 호출 필요
        type: "객관식",
        wrongCount: 1,
        correctCount: 0,
        analysis: "",
        attemptHistory: [
          {
            date: new Date(wrongAnswer.createdAt).toISOString().split("T")[0],
            isCorrect: wrongAnswer.isCorrect,
          },
        ],
        weakPoints: [],
        recommendations: [],
      });
      return acc;
    }, {} as Record<number, Category>);

    return Object.values(groupedByCategory);
  };

  // 컴포넌트 마운트 시 틀린 문제 데이터 가져오기
  useEffect(() => {
    const fetchWrongAnswers = async () => {
      try {
        const response = await readAllWrongAnswer();
        if (response.isSuccess && response.result) {
          const transformedCategories = transformWrongAnswersToCategories(response.result.allWrongAnswer);
          setCategories(transformedCategories);
        }
      } catch (error) {
        console.error("틀린 문제 데이터 가져오기 실패:", error);
      }
    };

    fetchWrongAnswers();
  }, [readAllWrongAnswer]);

  return {
    loading,
    error,
    categories,
    analyzeWrongAnswer,
    readAllWrongAnswer,
  };
};
