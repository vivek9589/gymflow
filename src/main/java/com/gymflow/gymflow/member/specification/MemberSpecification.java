package com.gymflow.gymflow.member.specification;


import com.gymflow.gymflow.member.entity.Member;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberSpecification {

    private MemberSpecification() {
    }

    public static Specification<Member> filterMembers(
            Long gymId,
            String status,
            String search,
            String planName,
            LocalDate today
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // -------------------------------------------------
            // Gym Isolation (Multi Tenant)
            // -------------------------------------------------

            predicates.add(
                    cb.equal(root.get("gym").get("id"), gymId)
            );

            // -------------------------------------------------
            // Soft Delete
            // -------------------------------------------------

            predicates.add(
                    cb.isFalse(root.get("deleted"))
            );

            // -------------------------------------------------
            // Status Filter
            // -------------------------------------------------

            if (status != null) {

                if (status.equalsIgnoreCase("ACTIVE")) {

                    predicates.add(
                            cb.equal(root.get("status"), "ACTIVE")
                    );

                    predicates.add(
                            cb.greaterThanOrEqualTo(
                                    root.get("expiryDate"),
                                    today
                            )
                    );

                }

                else if (status.equalsIgnoreCase("EXPIRED")) {

                    predicates.add(
                            cb.lessThan(
                                    root.get("expiryDate"),
                                    today
                            )
                    );

                }

                else {

                    predicates.add(
                            cb.equal(root.get("status"), status)
                    );

                }

            }

            // -------------------------------------------------
            // Search
            // -------------------------------------------------

            if (search != null && !search.isBlank()) {

                String keyword = "%" + search.toLowerCase() + "%";

                predicates.add(

                        cb.or(

                                cb.like(
                                        cb.lower(root.get("name")),
                                        keyword
                                ),

                                cb.like(
                                        root.get("phone"),
                                        "%" + search + "%"
                                ),

                                cb.like(
                                        cb.lower(root.get("email")),
                                        keyword
                                )

                        )

                );

            }

            // -------------------------------------------------
            // Plan Filter
            // -------------------------------------------------

            if (planName != null && !planName.isBlank()) {

                Join<Object, Object> planJoin =
                        root.join("currentPlan", JoinType.LEFT);

                predicates.add(

                        cb.equal(
                                planJoin.get("name"),
                                planName
                        )

                );

            }

            query.orderBy(

                    cb.desc(
                            root.get("createdAt")
                    )

            );

            return cb.and(

                    predicates.toArray(new Predicate[0])

            );

        };

    }

}