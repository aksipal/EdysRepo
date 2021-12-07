package com.via.ecza.repo;

import com.via.ecza.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    String SQL1 = "select * from category where code_value ILIKE :oldCodeValue%";
    @Query(value = SQL1,nativeQuery = true)
    List<Category> getSubCategoriesToCodeValue(@Param("oldCodeValue") String oldCodeValue);

    String SQL2 = "select count(*) from category where code=:code and category_parent_id=:categoryParentId";
    @Query(value = SQL2,nativeQuery = true)
    int findByCodeAndCategory(@Param("code") String code,
                              @Param("categoryParentId") Long categoryParentId);

    String SQL3 = "select count(*) from category where code=:code and category_parent_id is null";
    @Query(value = SQL3, nativeQuery = true)
    int findByCodeAndNullCategory(@Param("code") String code);

    String SQL4 = "select * from category c where c.code_value ILIKE '760%' OR c.code_value ILIKE '770%' OR c.code_value ILIKE '780%' order by c.code_value ";
    @Query(value = SQL4, nativeQuery = true)
    List<Category> getCategoriesForBuyUtilityInvoice();

    String SQL5 = "select * from category c where c.code_value ILIKE '191%' order by c.code_value ";
    @Query(value = SQL5, nativeQuery = true)
    List<Category> getVatCategoriesForBuyUtilityInvoice();

    String SQL6 = "select * from category c where c.code_value ILIKE '391%' order by c.code_value ";
    @Query(value = SQL6, nativeQuery = true)
    List<Category> getVatCategoriesForSellUtilityInvoice();

    String SQL7 = "select * from category c where c.code_value ILIKE '153%' order by c.code_value ";
    @Query(value = SQL7, nativeQuery = true)
    List<Category> getCommercialProductsCategories();

    String SQL8 = "select * from category c where c.code_value ILIKE '600%' order by c.code_value ";
    @Query(value = SQL8, nativeQuery = true)
    List<Category> getDomesticSellCategories();

    String SQL9 = "select * from category c where c.code_value ILIKE '120%' and checking_card_id=:checkingCardId order by c.code_value ";
    @Query(value = SQL9, nativeQuery = true)
    List<Category> searchTo120CheckingCardId(@Param("checkingCardId") Long checkingCardId);

    String SQL10 = "select * from category c where c.code_value ILIKE '320%' and checking_card_id=:checkingCardId order by c.code_value ";
    @Query(value = SQL10, nativeQuery = true)
    List<Category> searchTo320CheckingCardId(@Param("checkingCardId") Long checkingCardId);

    //hizmet satışı için gelecek olan 600 lü muhasebe kodları
    String SQL11 = "select * from category c where c.code_value ILIKE '6%' order by c.code_value ";
    @Query(value = SQL11, nativeQuery = true)
    List<Category> getCategoriesForSellUtilityInvoice();
}
