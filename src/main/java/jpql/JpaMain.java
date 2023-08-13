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
}
