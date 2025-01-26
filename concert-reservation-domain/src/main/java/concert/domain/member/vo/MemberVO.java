package concert.domain.member.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberVO {

    private final String uuid;
    private final String name;


    @Builder
    public MemberVO(String uuid, String name){
        this.uuid = uuid;
        this.name = name;
    }

    public static MemberVO of(String uuid, String name){
        return MemberVO.builder()
                .uuid(uuid)
                .name(name)
                .build();
    }
}
