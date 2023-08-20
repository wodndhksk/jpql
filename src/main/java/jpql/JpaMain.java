package jpql;

import javax.persistence.*;
import javax.persistence.criteria.From;
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
//            pagingEx(em);
//            joinEx(em);
//            typeEx(em);
//            caseEx(em);
//            coalesceAndNullIfEx(em);
//            jpqlFunctionEx(em);
//            fetchAndDistinctEx(em);
//            batchSizeEx(em);

            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

            List<Member> result = em.createNamedQuery("Member.findByUsername", Member.class)
                    .setParameter("username", "회원1")
                    .getResultList();

            for (Member member : result) {
                System.out.println("member = " + member);
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        em.close();
        emf.close();
    } // ================================================

    private static void batchSizeEx(EntityManager em) {
        Team teamA = new Team();
        teamA.setName("팀A");
        em.persist(teamA);

        Team teamB = new Team();
        teamB.setName("팀B");
        em.persist(teamB);

        Member member1 = new Member();
        member1.setUsername("회원1");
        member1.setTeam(teamA);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUsername("회원2");
        member2.setTeam(teamA);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUsername("회원3");
        member3.setTeam(teamB);
        em.persist(member3);

        em.flush();
        em.clear();

        String query = "select t from Team t"; //지연로딩을 사용해도 fetch 조인이 우선!
        List<Team> result = em.createQuery(query, Team.class)
                .setFirstResult(0)
                .setMaxResults(2)
                .getResultList();

        System.out.println("result = " + result.size());

        for (Team team : result) {
            System.out.println("team = " + team.getName() + "|members = " + team.getMember().size());
            for (Member member : team.getMember()){
                System.out.println("-> member = " + member);
            }
        }
    }

    private static void fetchAndDistinctEx(EntityManager em) {
        Team teamA = new Team();
        teamA.setName("팀A");
        em.persist(teamA);

        Team teamB = new Team();
        teamB.setName("팀B");
        em.persist(teamB);

        Member member1 = new Member();
        member1.setUsername("회원1");
        member1.setTeam(teamA);
        em.persist(member1);

        Member member2 = new Member();
        member2.setUsername("회원2");
        member2.setTeam(teamA);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUsername("회원3");
        member3.setTeam(teamB);
        em.persist(member3);

        em.flush();
        em.clear();

        // fetch 조인 : 한방 쿼리 (성능 향상 기대)
        // 그러나 일대다 fetch 조인에서는 데이터가 뻥튀기 되는 현상이 발생(중복값 존재) 따라서 distinct 를 사용하면 쿼리 뿐만 아니라
        // 애플리케이션단에서도 중복 제거
        String query = "select distinct t from Team t join fetch t.member"; //지연로딩을 사용해도 fetch 조인이 우선!
        List<Team> result = em.createQuery(query, Team.class)
                .getResultList();

//            for (Member member : result) {
//                System.out.println("member = " + member.getUsername() + ", team = " + member.getTeam().getName());
//                // 회원1, 팀A(SQL)
//                // 회원2, 팀A(1차캐시)
//                // 회원3, 팀B(SQL)
//            }

        System.out.println("result = " + result.size());

        for (Team team : result) {
            System.out.println("team = " + team.getName() + "|members = " + team.getMember().size());
            for (Member member : team.getMember()){
                System.out.println("-> member = " + member);
            }
        }
    }

    private static void jpqlFunctionEx(EntityManager em) {
        Member member1 = new Member();
        member1.setUsername("관리자");
        em.persist(member1);

        Member member2 = new Member();
        member2.setUsername("관리자");
        em.persist(member2);

        em.flush();
        em.clear();

//            String query = "select 'a' || 'b' From Member m";
//            String query = "select substring(m.username, 2, 3) From Member m";
//            String query = "select locate('de', 'abcdefg') From Member m"; // Integer 타입임
//            String query = "select size(t.member) From Team t";
//            String query = "select function('group_concat', m.username) From Member m"; //등록한 dialect 함수 사용
        String query = "select group_concat(m.username) From Member m"; // group_concat 은 hibernate 에서 지원하여 그냥 사용해도 됨


        List<String> result = em.createQuery(query, String.class).getResultList();
//            List<Integer> result = em.createQuery(query, Integer.class).getResultList();

        for (String s : result) {
            System.out.println("s = " + s);
        }

//            for (Integer s : result) {
//                System.out.println("s = " + s);
//            }
    }

    private static void coalesceAndNullIfEx(EntityManager em) {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("관리자");
        member.setAge(10);
        member.setTeam(team);
        member.setType(MemberType.ADMIN);
        em.persist(member);

        em.flush();
        em.clear();

        // coalesce : 하나씩 조회해서 null 이 아니면 반환 (null 이면 '이름없는 회원' 반환)
        String query1 = "select coalesce(m.username, '이름없는 회원') from Member m ";

        // m.username 이 '관리자' 이면 null 반환
        String query2 = "select nullif(m.username, '관리자') from Member m ";

        List<String> result = em.createQuery(query2, String.class)
                .getResultList();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    private static void caseEx(EntityManager em) {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member1");
        member.setAge(10);
        member.setTeam(team);
        member.setType(MemberType.ADMIN);
        em.persist(member);

        em.flush();
        em.clear();

        String query = "select " +
                "case when m.age <= 10 then '학생요금' " +
                "when m.age <= 60 then '경로요금' " +
                "else '일반요금' " +
                "end " +
                "from Member m";

        List<String> result = em.createQuery(query, String.class)
                .getResultList();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


    /**
     * JPQL 타입 표현과 기타식
     *
     * @param em
     */
    private static void typeEx(EntityManager em) {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member1");
        member.setAge(10);
        member.setTeam(team);
        member.setType(MemberType.ADMIN);
        em.persist(member);

        em.flush();
        em.clear();

        // MemberType 을 쿼리에 직접 입력시
//            String query ="select m.username, 'Hello', true from Member m " +
//                    "where m.type = jpql.MemberType.ADMIN";

        String query = "select m.username, 'Hello', true from Member m " +
                "where m.type = :userType";

        List<Object[]> result = em.createQuery(query)
                .setParameter("userType", MemberType.ADMIN)
                .getResultList();

        for (Object[] objects : result) {
            System.out.println("objects = " + objects[0]);
            System.out.println("objects = " + objects[1]);
            System.out.println("objects = " + objects[2]);
        }
    }

    /**
     * 조인
     *
     * @param em
     */
    private static void joinEx(EntityManager em) {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member1");
        member.setAge(10);
        member.setTeam(team);
        em.persist(member);

        em.flush();
        em.clear();

        // 연관관계 있는 내부 조인 (join -> m.team)
//            String query ="select m from Member m left join m.team t on t.name = 'teamA' ";
        // 연관관계 없는 외부 조인 (join -> Team t)
        String query = "select m from Member m left join Team t on t.name = 'teamA' ";

        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();
    }

    /**
     * 페이징
     *
     * @param em
     */
    private static void pagingEx(EntityManager em) {
        for (int i = 0; i < 100; i++) {
            Member member = new Member();
            member.setUsername("member" + i);
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
        for (Member m : result) {
            System.out.println("member = " + m);
        }
    }

    /**
     * 프로젝션
     *
     * @param em
     */
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

    /**
     * 기본 문법과 쿼리
     *
     * @param em
     */
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
