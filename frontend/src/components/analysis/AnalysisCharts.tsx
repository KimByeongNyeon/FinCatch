import React, { useState, useEffect } from 'react';
import { Doughnut, Line } from "react-chartjs-2";
import { 
  Chart as ChartJS, 
  CategoryScale, 
  LinearScale, 
  PointElement, 
  LineElement, 
  Title, 
  ArcElement, 
  Tooltip, 
  Legend,
  ChartOptions, 
  Scale, 
  CoreScaleOptions 
} from "chart.js";
import { Problem } from '../../types/analysis/Problem';
import axiosInstance from "../../api/axios";

// Chart.js에 필요한 컴포넌트들을 등록
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  ArcElement,
  Tooltip,
  Legend
);

interface AnalysisChartsProps {
  problem: Problem;
  isConsumption?: boolean; // 소비 퀴즈인지 여부를 결정하는 prop 추가
}

// 퀴즈 로그 인터페이스
interface QuizLog {
  quizLogId: number;
  memberId: number;
  userAnswer: string;
  isCorrect: boolean;
  createdAt: string;
}

const AnalysisCharts: React.FC<AnalysisChartsProps> = ({ problem, isConsumption = false }) => {
  // 문제 유형에 따라 사용자 답변과 정답 가져오기
  const userAnswer = (problem as any).userAnswer || '응답 없음';
  const correctAnswer = (problem as any).correctAnswer || '정보 없음';
  
  // 상태 관리
  const [quizLogs, setQuizLogs] = useState<QuizLog[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [correctRate, setCorrectRate] = useState<number>(0);
  const [totalAttempts, setTotalAttempts] = useState<number>(0);
  const [correctAttempts, setCorrectAttempts] = useState<number>(0);

  // 퀴즈 로그 조회 및 정답률 계산
  useEffect(() => {
    const fetchQuizLogs = async () => {
      if (!problem || !problem.id) return;
      
      setLoading(true);
      try {
        // 퀴즈 로그 API 호출
        const response = await axiosInstance.get(`/api/quiz/logs/${problem.id}`);
        
        if (response.data && response.data.isSuccess && Array.isArray(response.data.result)) {
          const logs = response.data.result as QuizLog[];
          setQuizLogs(logs);
          
          // 전체 시도 횟수와 정답 횟수 계산
          const total = logs.length;
          const correct = logs.filter(log => log.isCorrect).length;
          
          setTotalAttempts(total);
          setCorrectAttempts(correct);
          setCorrectRate(total > 0 ? (correct / total) * 100 : 0);
        }
      } catch (error) {
        console.error("퀴즈 로그 조회 오류:", error);
        // API 오류 시 attemptHistory 기반으로 계산
        calculateFromAttemptHistory();
      } finally {
        setLoading(false);
      }
    };
    
    // API 호출 실패 시 기존 attemptHistory 기반으로 계산
    const calculateFromAttemptHistory = () => {
      if (!problem.attemptHistory || problem.attemptHistory.length === 0) {
        setCorrectRate(0);
        setTotalAttempts(0);
        setCorrectAttempts(0);
        return;
      }
      
      const total = problem.attemptHistory.length;
      const correct = problem.attemptHistory.filter(attempt => attempt.isCorrect).length;
      
      setTotalAttempts(total);
      setCorrectAttempts(correct);
      setCorrectRate(total > 0 ? (correct / total) * 100 : 0);
    };
    
    fetchQuizLogs();
  }, [problem]);
  
  // 오답률 계산
  const wrongRate = 100 - correctRate;

  // 날짜만 표시하는 함수
  const formatDateOnly = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit'
    });
  };

  const doughnutData = {
    labels: ["정답", "오답"],
    datasets: [
      {
        data: [correctRate, wrongRate],
        backgroundColor: ["#4CAF50", "#FF5252"],
        borderWidth: 0,
      },
    ],
  };

  // 풀이 기록 데이터 - API 데이터 있으면 사용, 없으면 attemptHistory 사용
  const historyData = {
    labels: quizLogs.length > 0 
      ? quizLogs.map(log => formatDateOnly(log.createdAt)) 
      : problem.attemptHistory.map(h => formatDateOnly(h.date)),
    datasets: [
      {
        label: "문제 풀이 기록",
        data: quizLogs.length > 0 
          ? quizLogs.map(log => log.isCorrect ? 100 : 0)
          : problem.attemptHistory.map(h => h.isCorrect ? 100 : 0),
        borderColor: "#2196F3",
        tension: 0.1,
      },
    ],
  };

  const lineOptions: ChartOptions<"line"> = {
    scales: {
      y: {
        beginAtZero: true,
        max: 100,
        ticks: {
          callback: function (this: Scale<CoreScaleOptions>, tickValue: number | string) {
            const value = Number(tickValue);
            return value === 100 ? "정답" : value === 0 ? "오답" : "";
          },
        },
      },
    },
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-6">
        {isConsumption ? (
          // 소비 퀴즈의 경우 정답/오답 비교 UI
          <div className="bg-white p-4 rounded-lg shadow">
            <h5 className="font-korean-pixel text-gray-700 mb-4 text-center">정답 비교</h5>
            
            <div className="space-y-4">
              {/* 사용자 답변 */}
              <div className="bg-red-50 p-4 rounded-lg border-l-4 border-red-400">
                <div className="flex items-start">
                  <div className="mr-3 bg-red-100 rounded-full p-2 flex-shrink-0">
                    <svg className="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 font-korean-pixel">내 답변</p>
                    <p className="text-red-700 font-korean-pixel font-medium mt-1">{userAnswer}</p>
                  </div>
                </div>
              </div>
              
              {/* 올바른 답변 */}
              <div className="bg-green-50 p-4 rounded-lg border-l-4 border-green-400">
                <div className="flex items-start">
                  <div className="mr-3 bg-green-100 rounded-full p-2 flex-shrink-0">
                    <svg className="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 font-korean-pixel">정답</p>
                    <p className="text-green-700 font-korean-pixel font-medium mt-1">{correctAnswer}</p>
                  </div>
                </div>
              </div>
            </div>
            
            {/* 오답 카운트 표시 (선택적) */}
            {problem.wrongCount > 0 && (
              <div className="mt-4 flex justify-center">
                <div className="bg-gray-100 px-3 py-1 rounded-full">
                  <span className="text-sm text-gray-600 font-korean-pixel">
                    틀린 횟수: {problem.wrongCount}회
                  </span>
                </div>
              </div>
            )}
          </div>
        ) : (
          // 일반 문제(소비 퀴즈가 아닌 경우)는 기존 도넛 차트 유지
          <div className="bg-white p-4 rounded-lg shadow">
            <h5 className="font-korean-pixel text-gray-700 mb-4 text-center">정답률</h5>
            <div className="w-48 h-48 mx-auto">
              <Doughnut data={doughnutData} options={{ cutout: "70%" }} />
            </div>
            <div className="text-center mt-4">
              <span className="font-korean-pixel text-2xl font-bold text-blue-600">{correctRate.toFixed(1)}%</span>
            </div>
          </div>
        )}
        
        {/* 풀이 기록 그래프는 유지 */}
        <div className="bg-white p-4 rounded-lg shadow">
          <h5 className="font-korean-pixel text-gray-700 mb-4">풀이 기록</h5>
          {loading ? (
            <div className="flex justify-center items-center h-40">
              <div className="w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : (
            <Line data={historyData} options={lineOptions} />
          )}
        </div>
      </div>
    </div>
  );
};

export default AnalysisCharts;