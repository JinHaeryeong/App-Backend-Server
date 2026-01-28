import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 설정
export let options = {
    stages: [
        { duration: '10s', target: 50 }, // 10초 동안 사용자를 50명까지 서서히 늘림
        { duration: '30s', target: 150 }, // 30초 동안 150명 유지
        { duration: '10s', target: 0 },  // 10초 동안 종료
    ],
    thresholds: {
        http_req_duration: ['p(95)<200'], // 95%의 요청은 200ms 안에 들어와야 함 (성능 합격 기준)
    },
};

export default function () {
    // 쌤의 PC 실제 IP 주소를 넣어주세요!
    const url = 'http://192.168.219.106:8080/api/users';

    let res = http.get(url);

    // 응답 검증
    check(res, {
        'is status 200': (r) => r.status === 200,
        'body size > 0': (r) => r.body.length > 0,
    });

    // 너무 과한 부하 방지를 위해 아주 짧은 휴식 (0.1초)
    sleep(0.1);
}