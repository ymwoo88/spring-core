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

# 3. 스프링 핵심 원리 이해2 = 객체 지향 원리 적용

## 새로운 할인 정책 개발
* VIP는 퍼센트형태로 할인하는 로직 추가
```
package hello.core.discount;

import hello.core.member.Grade;
import hello.core.member.Member;

public class RateDiscountPolicy implements DiscountPolicy {

    private int discountPercent = 10;

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return price * discountPercent / 100;
        } else {
            return 0;
        }
    }
}

```

* 테스트 추가
```
package hello.core.discount;

import hello.core.member.Grade;
import hello.core.member.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateDiscountPolicyTest {

    RateDiscountPolicy discountPolicy = new RateDiscountPolicy();

    @Test
    @DisplayName("VIP는 10% 할인이 적용되어야 한다.")
    void vip_o() {
        //given
        Member member = new Member(1L, "memberVIP", Grade.VIP);
        //when
        int discount = discountPolicy.discount(member, 10000);
        //then
        Assertions.assertEquals(1000, discount);
    }

    @Test
    @DisplayName("VIP가 아니면 할인이 적용되지 않아야 한다.")
    void vip_x() {
        //given
        Member member = new Member(2L, "memberBASIC", Grade.BASIC);
        //when
        int discount = discountPolicy.discount(member, 10000);
        //then
        Assertions.assertEquals(0, discount);
    }
}
```

## 새로운 할인 정택 적용과 문제점
방금 추가한 할인 정택을 적용해보자

* 할인 정택을 애플리케이션에 적용해보자
  * 할인 정책을 변경하려면 클라이언트인 `OrderServiceImpl` 코드를 고쳐야 한다.
* 문제점 발견!!
  * 우리는 역활과 구현을 충실하게 분리했다. -> OK
  * 다형성도 활용하고, 인터페이스와 구현 객체를 분리했다 -> OK
  * OCP, CIP같은 객체지향 설계 원칙을 충실히 준수 했다
    * 그렇게 보이지만 사실은 아니다
  * DIP 주문 서비스 클라이언트 `OrderServiceImpl`은 `DiscountPolicy` 인터페이스에 의존하면서 DIP를 지킨 것같은데?
    * 클래스 의존과계를 분석해보자, 추상뿐만 아니라 구현 클래스에도 의존하고 있다.
      * 추상 의존 `DiscountPolicy`
      * 구현 클래스 `FixDiscountPolicy`, `RateDiscountPolicy`
  * OCP 변경하지 않고 확장할 수 있다고 했는데!?
    * 지금 코드는 기능을 확장해서 변경하면, 클라이언트 코드에 영향을 준다 따라서 OCP를 위반하고 있다.

문제점들을 잘 생각해보고 해결해나갈 방법을 생각해 보고 
다음시간에 답을 구해 보자!!

## 잠깐!!
애자일 소프트웨어 개발 선언문을 읽고 가자!
![img_1.png](img_1.png)
우리는 소프트웨어를 개발하고, 또 다른 사람의 개발을 도와주면서 소프트웨어 개발의 더 나은 방법들을 찾아가고 있다.
이작업을 통해 우리는 다음을 가지 있게 여기게 되었습니다.

공정과 도구보다 "개인과 상호작용" 을 포괄적인 문서보다 "작동하는 소프트웨어"를 꼐약 협상보다 "고객과의 협력"을 계획을 따르기보다 "변화에 대응하기"를
가지 있게 여긴다. 이 말은, 왼쪽에 있는 것들도 가치가 있지만, 우리는 오른쪽에 있는것들에 더 높은 가치를 둔다는 것이다.

## 관심사의 분리
* AppConfig를 통해서 관심사를 확실하게 분리했다.
* 배역, 배우를 생각해보자
* AppConfig는 공연 기획자다.
* AppConfig는 구체 클래스를 선택한다. 배역에 맞는 담당 배우를 선택한다, 애플리케이션이 어떻게 동작해야 할지 전체 구성을 책임진다.
* 이제 각 배우들은 담당 기능을 실행하는 책임만 지면 된다.
* orderServiceImpl 은 기능을 실행하는 책임만 지면 된다.


## AppCopnfig 리팩토링
현재 중복이 있고, 역활에 따른 구련이 잘 안보인다.
리팩터링 후
```
public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    private MemoryMemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }

    private FixDiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}
```


## 새로운 구조와 할인 정책 적용
* 처음으로 돌아가서 정책 할인 정책을 정률% 할인 정책으로 변경해보자
* FixDiscountPolicy -> RateDiscountPolicy
* 어떤 부분만 변경하면 되겠는가?

* AppConfig의 등장으로 애플리케이션이 크게 사용 영역과, 객체를 생성하고 구성하는 영역으로 분리되었다.

## 좋은 객체 지향 설계의 5가지 원칙의 적용
여기서 3가지 SRP, DIP, OCP 적용

SRP 단일 책임 원칙
한 클래스는 하나의 책임만 가져야 한다.

DIP 의존관계 역전 원칙
프로그래머는 “추상화에 의존해야지, 구체화에 의존하면 안된다.

OCP
소프트웨어 요소는 확장에는 열려 있으나 변경에는 닫여있어야 한다.
AppConfig가 의존관계를 FixDiscountPolicy > RateDiscountPolicy 로 변경해서 클라이언트 코드에 주입하므로 클라이언트 코드는 변경하지 않아도 됨
소프트웨어 요소를 새롭게 확장해도 사용 영역의 변경은 닫혀있다!!!
