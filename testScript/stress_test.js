import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 100,
    duration: '1m',
};

export default function () {
    const silverId = `test9999`;
    const url = 'http://localhost:8080/api/health/data';

    const now = new Date();

    // 10분 단위 데이터 생성 로직
    // 현재 시간에서 (반복 횟수 * 10분)씩 과거로 가는 데이터를 생성하여 중복을 피하기
    const tenMinutes = 10 * 60 * 1000;
    const offset = (__ITER % 100) * tenMinutes; // 유저당 최근 100개의 10분 단위 데이터 생성
    const fakeDate = new Date(now.getTime() - offset);

    const formattedDate = fakeDate.toISOString()
        .replace('T', ' ')      // 'T' -> ' '
        .split('.')[0];

    const payload = JSON.stringify({
        silverId: silverId,
        heartRateAvg: Math.floor(Math.random() * (100 - 60 + 1)) + 60,
        walkingSteps: Math.floor(Math.random() * 1000),
        totalCaloriesBurned: Math.random() * 500,
        spo2: 98,
        logDate: formattedDate, // 수정된 날짜 형식 전송
        sleepDurationMin: 480,
        sleepStageDeepMin: 60,
        sleepStageLightMin: 300,
        sleepStageRemMin: 120,
        sleepStageWakeMin: 0
    });

    const params = { headers: { 'Content-Type': 'application/json' } };
    let res = http.post(url, payload, params);

    check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'duration < 100ms': (r) => r.timings.duration < 100,
    });

    sleep(0.01);
}