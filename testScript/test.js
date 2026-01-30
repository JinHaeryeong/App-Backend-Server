import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '5s', target: 100 },  // 5초 동안 가상 유저 100명까지 증가 (빠른 확인을 위해 단축)
        { duration: '20s', target: 100 },  // 20초 동안 100명 유지
        { duration: '5s', target: 0 },   // 종료
    ],
};

export default function () {
    // 아까 새로 만든 테스트용 경로인 'sequence-test'로 변경
    // silverId 파라미터가 정확한지 확인
    let res = http.get('http://localhost:8080/api/health/sequence-test?silverId=test0001');

    check(res, {
        'status is 200': (r) => r.status === 200
    });

    sleep(0.1);
}