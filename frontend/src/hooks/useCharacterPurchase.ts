import { useState } from "react";
import { useApi } from "./useApi";
import { useUserInfo } from "./useUserInfo";
import { CustomAlert } from "../components/layout/CustomAlert";

interface Character {
  catId: number;
  catName: string;
  description: string;
  grade: string;
  isDuplicate?: boolean;
}

export const useCharacterPurchase = () => {
  const [isSpinning, setIsSpinning] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [pickedCharacters, setPickedCharacters] = useState<Character[]>([]);
  const [purchaseAmount, setPurchaseAmount] = useState(0);
  const [showAlert, setShowAlert] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");

  const { fetchUserInfo } = useUserInfo();
  const { execute } = useApi<Character[]>("/api/member/pick/cat", "GET");

  const handlePurchase = async (amount: number) => {
    console.log("구매 시작:", amount);
    setPurchaseAmount(amount);
    setIsSpinning(true);

    try {
      const response = await execute(undefined, {
        url: `/api/member/pick/cat?count=${amount}`,
        headers: {},
      });
      console.log("API 응답:", response);

      if (response.isSuccess && response.result) {
        console.log("뽑은 캐릭터들:", response.result);
        setPickedCharacters(response.result);
        // 뽑기 성공 후 유저 정보 갱신
        await fetchUserInfo();

        // 슬롯 애니메이션이 끝난 후 모달 표시
        setTimeout(() => {
          console.log("모달 표시 시도");
          setIsSpinning(false);
          setShowModal(true);
          console.log("상태 업데이트 후:", { isSpinning: false, showModal: true, pickedCharacters: response.result });
        }, 3000);
      } else {
        setIsSpinning(false);
        showCustomAlert("뽑기에 실패했습니다. 다시 시도해주세요.");
      }
    } catch (error) {
      console.error("뽑기 실패:", error);
      setIsSpinning(false);
      showCustomAlert("뽑기에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const closeModal = () => {
    console.log("모달 닫기");
    setShowModal(false);
    setPickedCharacters([]);
    setPurchaseAmount(0);
  };

  const showCustomAlert = (message: string) => {
    setAlertMessage(message);
    setShowAlert(true);
  };

  return {
    isSpinning,
    showModal,
    pickedCharacters,
    purchaseAmount,
    handlePurchase,
    closeModal,
    showAlert,
    alertMessage,
    setShowAlert,
  };
};
