package com.hcl.mi.helper;

import java.time.LocalDate;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.hcl.mi.entities.InspectionLot;

public class InspectionLotSpecifications {

    public static Specification<InspectionLot> withinDate(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.between(root.get("creationDate"), from, to);
    }

    public static Specification<InspectionLot> hasMaterialId(String materialId) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(materialId)) return cb.conjunction();

            // Null-safe LEFT JOIN to material
            Join<Object, Object> material = root.join("material", JoinType.LEFT);

            // Normalize by trimming & upper-casing on DB side if your JPA provider supports it
            // Fallback: compare as-is (but better to normalize the input)
            String normalized = materialId.replaceAll("\\s+", "").toUpperCase();

            // If your column stores normalized IDs (no spaces, uppercase), use equality.
            // Otherwise, apply functions. Not all dialects support TRIM/UPPER on nested fields with spaces removed.
            // A safe approach: use upper and remove spaces in Java for input; assume DB is stored normalized.
            return cb.equal(cb.upper(material.get("materialId")), normalized);
        };
    }

    public static Specification<InspectionLot> hasPlantId(String plantId) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(plantId)) return cb.conjunction();

            Join<Object, Object> plant = root.join("plant", JoinType.LEFT);
            String normalized = plantId.trim().toUpperCase();
            return cb.equal(cb.upper(plant.get("plantId")), normalized);
        };
    }

    public static Specification<InspectionLot> hasStatus(String status) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(status)) return cb.conjunction();
            return cb.equal(root.get("result"), status);
        };
    }

    public static Specification<InspectionLot> hasVendorId(Integer vendorId) {
        return (root, query, cb) -> {
        	 if (vendorId == null || vendorId <= 0) return cb.conjunction();
        	 Join<Object, Object> vendor = root.join("vendor", JoinType.LEFT);
            return cb.equal(vendor.get("vendorId"), vendorId);
        };
    }
}
