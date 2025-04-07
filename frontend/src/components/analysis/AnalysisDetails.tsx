import React, { useMemo } from 'react';
import { Problem } from '../../types/analysis/Problem';
import AnalysisCharts from './AnalysisCharts';

interface AnalysisDetailsProps {
  problem: Problem;
  loading: boolean;
  error: boolean;  // 명확한 boolean 타입으로 정의
}

const AnalysisDetails: React.FC<AnalysisDetailsProps> = ({ problem, loading, error }) => {
  // 로딩 상태 컴포넌트
  const renderLoadingState = () => (
    <div className="flex items-center justify-center p-4">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      <span className="ml-2 font-korean-pixel">분석 중...</span>
    </div>
  );

  // 에러 상태 컴포넌트
  const renderErrorState = () => (
    <div className="text-red-500 p-4 text-center font-korean-pixel">
      분석 중 오류가 발생했습니다. 다시 시도해주세요.
    </div>
  );

  // 소비 퀴즈 오답인지 확인 (문제 분석에 "소비 퀴즈"라는 문구가 포함되어 있는지 확인)
  const isConsumptionQuiz = problem.analysis.includes('소비 퀴즈') || 
                           problem.weakPoints.some(point => point.includes('소비 퀴즈'));
                           
  // 금융 공부 팁 목록
  const financeTips = [
    // 새로 추가된 11개의 상세 팁
    "기초 개념부터 차근차근 이해하세요. 금융 공부를 시작할 땐 '복리', '인플레이션', '자산과 부채' 같은 기초 개념을 먼저 이해하는 것이 가장 중요합니다. 기본 용어조차 제대로 알지 못한 상태로 복잡한 투자나 금융상품에 접근하면, 헷갈리기 쉽고 실수도 잦아져요. 용어 하나하나를 사전처럼 정리해두거나, 머릿속에서 흐름으로 연결하는 연습을 해보세요.",
    "사례 중심으로 시사 이슈를 접해보세요. 금융은 현실과 맞닿아 있는 분야라, 현재 어떤 일이 벌어지고 있는지를 알아야 공부가 생생해집니다. 예를 들어 '기준금리 인상'이라는 뉴스를 보면, 왜 시장이 흔들리는지, 나의 예적금엔 어떤 영향이 있을지를 연결해보는 것이죠.",
    "실생활에 적용하며 반복 학습하세요. 공부한 내용을 바로 실생활에 적용해보는 것이 가장 확실한 복습이자 체화 방법이에요. 예를 들어, 카드 명세서의 이자율, 은행 앱의 예적금 금리, 대출 상품 비교 등을 직접 해보며 실전 감각을 익힐 수 있습니다.",
    "핵심 개념을 정리하고 복습하세요. 금융 용어나 개념은 반복하지 않으면 금세 잊히기 때문에, 짧게라도 자주 복습하는 것이 중요해요. 노트를 만들어 핵심 개념을 한 줄 요약하고, 옆에 관련 예시나 실제 뉴스 사례를 적어보는 방식이 좋아요.",
    "실제 금융 뉴스에서 배운 내용을 찾아보세요. 금융 개념을 익힌 뒤에는, 그것이 실제 시장에서 어떻게 사용되는지를 직접 확인해보는 게 정말 중요해요. 예를 들어 '금리가 인상되면 채권 가격이 떨어진다'는 개념을 알게 되었다면, 관련 뉴스 기사에서 그런 사례를 찾아보는 거죠.",
    "금융 콘텐츠를 꾸준히 들어보세요. 금융 유튜브, 팟캐스트, 오디오북은 공부에 지루함을 느낄 틈을 줄여주는 훌륭한 도구예요. 특히 출퇴근 시간, 운동할 때, 설거지하거나 정리할 때 틀어놓기만 해도 귀로 자연스럽게 익숙해집니다.",
    "가계부로 돈의 흐름을 직접 체감해보세요. 가계부를 쓰는 건 단순한 기록이 아니라, 나의 '재무 상태'를 눈으로 확인하고 관리하는 훈련입니다. 내가 어디에 가장 많이 쓰고 있는지, 고정지출과 변동지출은 어떻게 구성돼 있는지를 파악해보세요.",
    "작게 쪼개서 한 주제씩 집중하세요. 금융은 범위가 넓고 용어도 많아서, 처음엔 막막할 수 있어요. 이럴 땐 하루에 한 주제씩만 공부해보는 게 좋아요. 예를 들어 오늘은 '예금과 적금의 차이', 내일은 '신용카드의 이자 구조', 모레는 'ETF란 무엇인가'처럼요.",
    "모의투자로 시장의 흐름을 익혀보세요. 실제로 돈을 걸지 않아도, 요즘은 다양한 모의투자 플랫폼이 있어서 가상으로 투자 경험을 쌓을 수 있어요. 주식, 채권, ETF, 펀드 등을 가상으로 사고팔며 시장의 흐름을 직접 체험해볼 수 있죠.",
    "재테크 책 한 권을 끝까지 읽어보세요. 정보가 넘쳐나는 시대지만, 깊이 있는 이해를 위해서는 책만큼 좋은 자료가 없어요. 특히 입문서 한 권을 처음부터 끝까지 정독하는 경험은 금융 공부의 기준점을 만들어줍니다.",
    "경제 신문이나 뉴스 앱으로 흐름을 체크하세요. 매일 아침 경제 뉴스 한 꼭지만이라도 읽는 습관은 금융 감각을 빠르게 키워주는 방법입니다. 모든 기사를 다 읽을 필요는 없고, 헤드라인과 요약을 통해 흐름만이라도 잡아보세요.",
  ];
  
  // 랜덤하게 팁 선택 (컴포넌트가 리렌더링될 때마다 변경되지 않도록 useMemo 사용)
  const randomTip = useMemo(() => {
    if (!isConsumptionQuiz) return null;
    const randomIndex = Math.floor(Math.random() * financeTips.length);
    return financeTips[randomIndex];
  }, [isConsumptionQuiz, problem.id]); // problem.id를 의존성 배열에 추가하여 문제가 바뀔 때마다 새로운 팁이 선택되도록 함

  if (loading) {
    return renderLoadingState();
  }

  if (error) {
    return renderErrorState();
  }

  return (
    <div className="space-y-6">
      <div>
        <h4 className="font-korean-pixel text-lg text-gray-800 mb-4">{problem.title}</h4>
        <AnalysisCharts problem={problem} />
      </div>
      <div className="mt-6">
        <h5 className="font-korean-pixel text-gray-800 mb-2">📊 분석 내용</h5>
        <p className="font-korean-pixel text-gray-600 whitespace-pre-line">{problem.analysis}</p>
      </div>
      <div>
        {/* <h5 className="font-korean-pixel text-gray-800 mb-2">💡 취약점</h5> */}
        <ul className="list-disc pl-5 space-y-1">
        </ul>
      </div>
      <div>
        {/* <h5 className="font-korean-pixel text-gray-800 mb-2">✨ 추천 학습</h5> */}
        <ul className="list-disc pl-5 space-y-1">
        </ul>
      </div>
      
      {/* 금융 공부 조언 섹션 - 소비 퀴즈 오답일 때만 표시 */}
      {isConsumptionQuiz && randomTip && (
        <div className="mt-4">
          <p className="font-korean-pixel text-gray-700">
            {randomTip}
          </p>
        </div>
      )}
    </div>
  );
};

export default AnalysisDetails;