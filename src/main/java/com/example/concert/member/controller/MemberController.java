package com.example.concert.member.controller;


import com.example.concert.member.service.MemberService;
import com.example.concert.token.dto.TokenRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/member/token")
    public void createToken(@RequestBody TokenRequest tokenRequest){
        long uuid = tokenRequest.getUuid();
        memberService.createToken(uuid);
    }
}
