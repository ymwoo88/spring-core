package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;

public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();

    public OrderServiceImpl(MemoryMemberRepository memoryMemberRepository, FixDiscountPolicy fixDiscountPolicy) {
    }

    @Override
    public Order createOrder(Long memberId, String itemName, int itemPlace) {

        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPlace);

        return new Order(memberId, itemName, itemPlace, discountPrice);
    }
}
