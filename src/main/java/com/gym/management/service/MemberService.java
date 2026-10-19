package com.gym.management.service;

import com.gym.management.model.Member;
import com.gym.management.model.MemberStatus;
import com.gym.management.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public List<Member> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return memberRepository.findAll();
        }
        return memberRepository.searchMembers(query.trim());
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }

    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    public long countActiveMembers() {
        return memberRepository.countByStatus(MemberStatus.ACTIVE);
    }

    public long countTotalMembers() {
        return memberRepository.count();
    }
}
