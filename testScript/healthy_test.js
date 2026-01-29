import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 50,          // 안정적인 테스트를 위해 VU는 50으로 살짝 조절
    duration: '1m',
};

export default function () {
    const silverId = `tester100`; // 새로운 ID로 깨끗하게 시작
    const url = 'http://localhost:8080/api/health/data';

    // 10분 단위 날짜 생성 (서버 패턴 "yyyy-MM-dd HH:mm:ss" 맞춤)
    const now = new Date();
    const tenMinutes = 10 * 60 * 1000;
    const offset = (__ITER % 100) * tenMinutes;
    const fakeDate = new Date(now.getTime() - offset);

    const formattedDate = fakeDate.toISOString()
        .replace('T', ' ')
        .split('.')[0];

    // 건강하고 평온한 데이터 세팅 => Nomal 라벨링 확인용
    const payload = JSON.stringify({
        silverId: silverId,
        heartRateAvg: Math.floor(Math.random() * (75 - 65 + 1)) + 65, // 안정 시 심박수 (65~75)
        walkingSteps: Math.floor(Math.random() * 50),                 // 아주 천천히 걷거나 쉬는 중
        totalCaloriesBurned: 1.5,                                     // 낮은 활동량
        spo2: 99,                                                     // 최상의 산소포화도
        logDate: formattedDate,

        // 수면 지표도 이상적인 비율로 설정 (Deep 20%, Rem 20% 등)
        sleepDurationMin: 480,   // 8시간 꿀잠
        sleepStageDeepMin: 96,   // 깊은 수면 충분
        sleepStageLightMin: 280,
        sleepStageRemMin: 100,
        sleepStageWakeMin: 4     // 뒤척임 거의 없음
    });

    const params = { headers: { 'Content-Type': 'application/json' } };
    let res = http.post(url, payload, params);

    check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'duration < 100ms': (r) => r.timings.duration < 100,
    });

    sleep(0.05); // 서버가 하나씩 분석할 시간을 줌
}