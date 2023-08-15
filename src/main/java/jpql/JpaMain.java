package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
//            startJpql(em);
//            projectionEx(em);

            for(int i=0; i<100; i++){
                Member member = new Member();
                member.setUsername("member"+i);
                member.setAge(i);
                em.persist(member);
            }

            em.flush();
            em.clear();

            List<Member> result = em.createQuery("select m from Member m order by m.age desc ")
                    .setFirstResult(1)
                    .setMaxResults(10)
                    .getResultList();

            System.out.println("result.age = " + result.size());
            for(Member m : result){
                System.out.println("member = " + m);
            }


            em.flush();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        em.close();
        emf.close();
    }

    private static void projectionEx(EntityManager em) {
        Member member = new Member();
        member.setUsername("member1");
        em.persist(member);

        em.flush();
        em.clear();

        List<Member> result = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        Member findMember = result.get(0);
        findMember.setAge(20);

        em.flush();
        em.clear();

        // 일반 select로 team 조회시 조인문이 생성되므로 명시적으로 표현하기 위해 jpql 자체에서 join 문을 사용하는것을 권장.
        List<Team> result2 = em.createQuery("select t from Member m join m.team t", Team.class)
                .getResultList();

        // Embedded 타입 사용
        em.createQuery("select o.address from Order o ", Address.class)
                        .getResultList();

        //select 값이 여러개일때
        em.createQuery("select distinct m.username, m.age from Member m")
                        .getResultList();
        List<Object[]> result3 = em.createQuery("select m.username, m.age from Member m")
                .getResultList();
        Object[] objects = result3.get(0);
        System.out.println("username = " + objects[0]);
        System.out.println("age = " + objects[1]);

        // MemberDto 에 담기
        List<MemberDto> result4 = em.createQuery("select new jpql.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();
        MemberDto memberDto = result4.get(0);
        System.out.println("memberDto.getUsername = " + memberDto.getUsername());
        System.out.println("memberDto.getAge = " + memberDto.getAge());
    }

    private static void startJpql(EntityManager em) {
        Member member = new Member();
        member.setUsername("member1");
        em.persist(member);
        /**
         * 조회하는 값의 타입에 따라 다르게 리턴됨
         */
        TypedQuery<Member> query1 = em.createQuery("select m from Member m ", Member.class);
        TypedQuery<String> query2 = em.createQuery("select m.username from Member m ", String.class);
        Query query3 = em.createQuery("select m.username, m.age from Member m ");
        List<Member> resultList = query1.getResultList(); // 결과가 하나 이상 일때
        Member singleResult = query1.getSingleResult(); // 결과가 정확히 하나 일때

        /**
         * 파라미터 바인딩
         */
        List result = em.createQuery("select m from Member m where m.username = :username")
                .setParameter("username", "김상덕")
                .getResultList();
    }
}
