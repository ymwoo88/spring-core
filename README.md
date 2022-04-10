# spring-core
김영학의 인프런 강좌를 보면서 실습

# 스프링 핵심 원리 - 기본편

## 비즈니스 요규사항과 설계
* 회원
  * 회원을 가입하고 조회할 수 있다.
  * 회원은 일반과 VIP 두 가지 등급이 있다.
  * 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)
* 주문과 할인 정책
  * 회원은 상품을 주문할 수 있다.
  * 회원 등급에 따라 할인 정책을 적용할 수 있다.
  * 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경 될 수 있다.)
  * 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수도 있다. (미확정)


요구사항을 보면 회원 데이터, 할인 정책 같은 부분은 지금 결정하지 어려 부분이다. 그렇다고 이런 정책이 결정 될 때 까지 개발을 무기한 기다릴 수 도 없다.  
우리는 앞에서 배운 객체 지향 설계 방법이 있으니까 그것을 활용해볼 계획이다.

인퍼페이스를 만들고 구현체를 언제든지 갈아끼울 수 있도록 설계하면 된다. 그럼 시작해보자

참고: 프로젝트 환경설정을 편리하게 하려고 스프링 부트를 사용한 것이다. 지금은 스프링이 없는 순수한 자바로만 개발을 진행한다는  
점을 꼭 기억하자! 스프링 관련은 한참 뒤에 등장한다.

## 회원 도메인 설계
* 회원 도메인 요구사항
  * 회원을 가입하고 조회할 수 있다.
  * 회원은 일반과 VIP 두 가지 등급이 있다.
  * 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)

>![img.png](img.png)

## 회원 도메인 개발
* 모델 생성
```
package hello.core.member;

public class Member {

    private Long id;
    private String name;
    private Grade grade;

    public Member(Long id, String name, Grade grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }
}

```
```
package hello.core.member;

public enum Grade {
    BASIC,
    VIP,
    ;
}

```

* 서비스 
```
package hello.core.member;

public interface MemberService {

    void join(Member member);

    Member findMember(Long memberId);
}

---------------------------------------------------------------
package hello.core.member;

public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository = new MemoryMemberRepository();

    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}

```
* 레포지토리 
```
package hello.core.member;

public interface MemberRepository {

    void save(Member member);

    Member findById(Long memberId);
}
-----------------------------------------------------------------
package hello.core.member;

import java.util.HashMap;
import java.util.Map;

public class MemoryMemberRepository implements MemberRepository {

    /**
     * 동시성 이슈로 원래는 ConcurrentHashMap을 써야한다.
     */
    private static Map<Long, Member> store = new HashMap<>();

    @Override
    public void save(Member member) {
        store.put(member.getId(), member);
    }

    @Override
    public Member findById(Long memberId) {
        return store.get(memberId);
    }
}

```

## 회원 도메인 실행과 테스트
* 서비스 테스트 생성
```
package hello.core.member;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MemberServiceTest {

    MemberService memberService = new MemberServiceImpl();

    @Test
    void join() {
        //given
        Member member = new Member(1L, "A", Grade.VIP);

        //when
        memberService.join(member);
        Member findMember = memberService.findMember(1L);

        //then
        Assertions.assertEquals(member, findMember);
    }
}

```
* 질문 사항
  * 이 코드의 설계상 문제점은 무엇일까요?
  * 다른 저장소로 변경할 때 OCP 원칙을 잘 준수 할 수 있을까요?
  * DIP를 잘 지키고 있을까요?
  * 의존관계가 인터페이스 뿐만 아니라 구현까지 모두 의존하는 문제점이 있음
    * 주문까지 만들고나서 문제점과 해결 방안을 설정


## 주문과 할인 도메인 설계
* 주문과 할인 정택
  * 회원은 상품을 주문할 수 있다.
  * 회원 등급에 따라 할인 정책을 적용 할 수 있다.
  * 할인 정택은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경 될 수 있다.)
  * 할인 정택은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수도 있다. (미확정)
  * (이미지)
  
1. 주문 생성 : 클라이언트는 주문 서비스에 주문 생생을 한다.
2. 회원 조회 : 할인을 위해서는 회원 등급이 필요하다. 그래서 주문 서비스는 회원 저장소에서 회원을 조회한다.
3. 할인 적용 : 주문 서비스는 회원 등급에 따른 할인 여부를 할인 정택에 위임한다.
4. 주문 결과 반환 : 주문 서비스는 할인 결과를 포함한 주문 결과를 반환한다.
> 참고 : 실제로는 주문 데이터를 DB에 저장하지만, 예제가 너무 복잡해 질 수 있어서 생략하고, 단순히 주문 결과를 반환한다.

(이미지)

역활과 구현을 분리해서 자유롭게 구현체를 조립할 수 있게 설계했다. 덕분에 회원 저장소를 물론이고, 할인 정택도 유연하게 변경할 수 있다.

* 주문 도메인 객체 다이러그램1
  * 클라이언트
    * -> 주문 서비스 구현체
      * -> 메모리 회원 저장소
      * -> 정액 할인 정책

회원을 메모리에서 조회하고, 정액 할인 정택(고정 금액)을 지원해도 주문 서비스를 변경하지 않아도 된다.
역확들을 협력 관계를 그대로 재사용할 수 있다.

* 주문 도메인 객체 다이러그램2
  * 클라이언트
    * -> 주문 서비스 구현체
      * -> DB 회원 저장소
      * -> 정액 할인 정책

회원을 메모리가 아닌 실제 DB에서 조회하고, 정률 할인 정택(주문 금액에 따라 & 할인)을 지원해도 주문 서비스를 변경하지 않아도 된다.
협력 관계를 그대로 재사용할 수 있다.

## 주문과 할인 도메인 개발
* 주문 모델
```
package hello.core.order;

public class Order {

    private Long memberId;
    private String itemName;
    private int itemPrice;
    private int discountPrice;

    public Order(Long memberId, String itemName, int itemPrice, int discountPrice) {
        this.memberId = memberId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPrice = discountPrice;
    }

    public int calculatePrice() {
        return itemPrice - discountPrice;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "memberId=" + memberId +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", discountPrice=" + discountPrice +
                '}';
    }
}

```

* 주문 서비스
```
package hello.core.order;

public interface OrderService {
    
    Order createOrder(Long memberId, String itemName, int itemPlace);
}

----------------------------------------------------------------------
package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;

public class OrderServiceImpl implements OrderService {
    
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    
    @Override
    public Order createOrder(Long memberId, String itemName, int itemPlace) {
        
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPlace);
        
        return new Order(memberId, itemName, itemPlace, discountPrice);
    }
}

```

* 할인 정책 비니지스
```
package hello.core.discount;

import hello.core.member.Member;

public interface DiscountPolicy {

    int discount(Member member, int price);
}
------------------------------------------------------------------------
package hello.core.discount;

import hello.core.member.Grade;
import hello.core.member.Member;

public class FixDiscountPolicy implements DiscountPolicy {

    private int discountFicAmount = 1000;

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return discountFicAmount;
        } else {
            return 0;
        }
    }
}
```

## 주문과 할인 도메인 실행과 테스트
```
package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;

public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPlace) {

        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPlace);

        return new Order(memberId, itemName, itemPlace, discountPrice);
    }
}

```