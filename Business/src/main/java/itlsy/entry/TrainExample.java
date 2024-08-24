package itlsy.entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrainExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TrainExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andTrainNumberIsNull() {
            addCriterion("train_number is null");
            return (Criteria) this;
        }

        public Criteria andTrainNumberIsNotNull() {
            addCriterion("train_number is not null");
            return (Criteria) this;
        }

        public Criteria andTrainNumberEqualTo(String value) {
            addCriterion("train_number =", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberNotEqualTo(String value) {
            addCriterion("train_number <>", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberGreaterThan(String value) {
            addCriterion("train_number >", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberGreaterThanOrEqualTo(String value) {
            addCriterion("train_number >=", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberLessThan(String value) {
            addCriterion("train_number <", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberLessThanOrEqualTo(String value) {
            addCriterion("train_number <=", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberLike(String value) {
            addCriterion("train_number like", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberNotLike(String value) {
            addCriterion("train_number not like", value, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberIn(List<String> values) {
            addCriterion("train_number in", values, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberNotIn(List<String> values) {
            addCriterion("train_number not in", values, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberBetween(String value1, String value2) {
            addCriterion("train_number between", value1, value2, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainNumberNotBetween(String value1, String value2) {
            addCriterion("train_number not between", value1, value2, "trainNumber");
            return (Criteria) this;
        }

        public Criteria andTrainTypeIsNull() {
            addCriterion("train_type is null");
            return (Criteria) this;
        }

        public Criteria andTrainTypeIsNotNull() {
            addCriterion("train_type is not null");
            return (Criteria) this;
        }

        public Criteria andTrainTypeEqualTo(Integer value) {
            addCriterion("train_type =", value, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeNotEqualTo(Integer value) {
            addCriterion("train_type <>", value, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeGreaterThan(Integer value) {
            addCriterion("train_type >", value, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("train_type >=", value, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeLessThan(Integer value) {
            addCriterion("train_type <", value, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeLessThanOrEqualTo(Integer value) {
            addCriterion("train_type <=", value, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeIn(List<Integer> values) {
            addCriterion("train_type in", values, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeNotIn(List<Integer> values) {
            addCriterion("train_type not in", values, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeBetween(Integer value1, Integer value2) {
            addCriterion("train_type between", value1, value2, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("train_type not between", value1, value2, "trainType");
            return (Criteria) this;
        }

        public Criteria andTrainTagIsNull() {
            addCriterion("train_tag is null");
            return (Criteria) this;
        }

        public Criteria andTrainTagIsNotNull() {
            addCriterion("train_tag is not null");
            return (Criteria) this;
        }

        public Criteria andTrainTagEqualTo(String value) {
            addCriterion("train_tag =", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagNotEqualTo(String value) {
            addCriterion("train_tag <>", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagGreaterThan(String value) {
            addCriterion("train_tag >", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagGreaterThanOrEqualTo(String value) {
            addCriterion("train_tag >=", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagLessThan(String value) {
            addCriterion("train_tag <", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagLessThanOrEqualTo(String value) {
            addCriterion("train_tag <=", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagLike(String value) {
            addCriterion("train_tag like", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagNotLike(String value) {
            addCriterion("train_tag not like", value, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagIn(List<String> values) {
            addCriterion("train_tag in", values, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagNotIn(List<String> values) {
            addCriterion("train_tag not in", values, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagBetween(String value1, String value2) {
            addCriterion("train_tag between", value1, value2, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainTagNotBetween(String value1, String value2) {
            addCriterion("train_tag not between", value1, value2, "trainTag");
            return (Criteria) this;
        }

        public Criteria andTrainBrandIsNull() {
            addCriterion("train_brand is null");
            return (Criteria) this;
        }

        public Criteria andTrainBrandIsNotNull() {
            addCriterion("train_brand is not null");
            return (Criteria) this;
        }

        public Criteria andTrainBrandEqualTo(String value) {
            addCriterion("train_brand =", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandNotEqualTo(String value) {
            addCriterion("train_brand <>", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandGreaterThan(String value) {
            addCriterion("train_brand >", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandGreaterThanOrEqualTo(String value) {
            addCriterion("train_brand >=", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandLessThan(String value) {
            addCriterion("train_brand <", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandLessThanOrEqualTo(String value) {
            addCriterion("train_brand <=", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandLike(String value) {
            addCriterion("train_brand like", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandNotLike(String value) {
            addCriterion("train_brand not like", value, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandIn(List<String> values) {
            addCriterion("train_brand in", values, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandNotIn(List<String> values) {
            addCriterion("train_brand not in", values, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandBetween(String value1, String value2) {
            addCriterion("train_brand between", value1, value2, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andTrainBrandNotBetween(String value1, String value2) {
            addCriterion("train_brand not between", value1, value2, "trainBrand");
            return (Criteria) this;
        }

        public Criteria andStartStationIsNull() {
            addCriterion("start_station is null");
            return (Criteria) this;
        }

        public Criteria andStartStationIsNotNull() {
            addCriterion("start_station is not null");
            return (Criteria) this;
        }

        public Criteria andStartStationEqualTo(String value) {
            addCriterion("start_station =", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationNotEqualTo(String value) {
            addCriterion("start_station <>", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationGreaterThan(String value) {
            addCriterion("start_station >", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationGreaterThanOrEqualTo(String value) {
            addCriterion("start_station >=", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationLessThan(String value) {
            addCriterion("start_station <", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationLessThanOrEqualTo(String value) {
            addCriterion("start_station <=", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationLike(String value) {
            addCriterion("start_station like", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationNotLike(String value) {
            addCriterion("start_station not like", value, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationIn(List<String> values) {
            addCriterion("start_station in", values, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationNotIn(List<String> values) {
            addCriterion("start_station not in", values, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationBetween(String value1, String value2) {
            addCriterion("start_station between", value1, value2, "startStation");
            return (Criteria) this;
        }

        public Criteria andStartStationNotBetween(String value1, String value2) {
            addCriterion("start_station not between", value1, value2, "startStation");
            return (Criteria) this;
        }

        public Criteria andEndStationIsNull() {
            addCriterion("end_station is null");
            return (Criteria) this;
        }

        public Criteria andEndStationIsNotNull() {
            addCriterion("end_station is not null");
            return (Criteria) this;
        }

        public Criteria andEndStationEqualTo(String value) {
            addCriterion("end_station =", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationNotEqualTo(String value) {
            addCriterion("end_station <>", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationGreaterThan(String value) {
            addCriterion("end_station >", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationGreaterThanOrEqualTo(String value) {
            addCriterion("end_station >=", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationLessThan(String value) {
            addCriterion("end_station <", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationLessThanOrEqualTo(String value) {
            addCriterion("end_station <=", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationLike(String value) {
            addCriterion("end_station like", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationNotLike(String value) {
            addCriterion("end_station not like", value, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationIn(List<String> values) {
            addCriterion("end_station in", values, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationNotIn(List<String> values) {
            addCriterion("end_station not in", values, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationBetween(String value1, String value2) {
            addCriterion("end_station between", value1, value2, "endStation");
            return (Criteria) this;
        }

        public Criteria andEndStationNotBetween(String value1, String value2) {
            addCriterion("end_station not between", value1, value2, "endStation");
            return (Criteria) this;
        }

        public Criteria andStartRegionIsNull() {
            addCriterion("start_region is null");
            return (Criteria) this;
        }

        public Criteria andStartRegionIsNotNull() {
            addCriterion("start_region is not null");
            return (Criteria) this;
        }

        public Criteria andStartRegionEqualTo(String value) {
            addCriterion("start_region =", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionNotEqualTo(String value) {
            addCriterion("start_region <>", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionGreaterThan(String value) {
            addCriterion("start_region >", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionGreaterThanOrEqualTo(String value) {
            addCriterion("start_region >=", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionLessThan(String value) {
            addCriterion("start_region <", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionLessThanOrEqualTo(String value) {
            addCriterion("start_region <=", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionLike(String value) {
            addCriterion("start_region like", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionNotLike(String value) {
            addCriterion("start_region not like", value, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionIn(List<String> values) {
            addCriterion("start_region in", values, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionNotIn(List<String> values) {
            addCriterion("start_region not in", values, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionBetween(String value1, String value2) {
            addCriterion("start_region between", value1, value2, "startRegion");
            return (Criteria) this;
        }

        public Criteria andStartRegionNotBetween(String value1, String value2) {
            addCriterion("start_region not between", value1, value2, "startRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionIsNull() {
            addCriterion("end_region is null");
            return (Criteria) this;
        }

        public Criteria andEndRegionIsNotNull() {
            addCriterion("end_region is not null");
            return (Criteria) this;
        }

        public Criteria andEndRegionEqualTo(String value) {
            addCriterion("end_region =", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionNotEqualTo(String value) {
            addCriterion("end_region <>", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionGreaterThan(String value) {
            addCriterion("end_region >", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionGreaterThanOrEqualTo(String value) {
            addCriterion("end_region >=", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionLessThan(String value) {
            addCriterion("end_region <", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionLessThanOrEqualTo(String value) {
            addCriterion("end_region <=", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionLike(String value) {
            addCriterion("end_region like", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionNotLike(String value) {
            addCriterion("end_region not like", value, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionIn(List<String> values) {
            addCriterion("end_region in", values, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionNotIn(List<String> values) {
            addCriterion("end_region not in", values, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionBetween(String value1, String value2) {
            addCriterion("end_region between", value1, value2, "endRegion");
            return (Criteria) this;
        }

        public Criteria andEndRegionNotBetween(String value1, String value2) {
            addCriterion("end_region not between", value1, value2, "endRegion");
            return (Criteria) this;
        }

        public Criteria andSaleTimeIsNull() {
            addCriterion("sale_time is null");
            return (Criteria) this;
        }

        public Criteria andSaleTimeIsNotNull() {
            addCriterion("sale_time is not null");
            return (Criteria) this;
        }

        public Criteria andSaleTimeEqualTo(Date value) {
            addCriterion("sale_time =", value, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeNotEqualTo(Date value) {
            addCriterion("sale_time <>", value, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeGreaterThan(Date value) {
            addCriterion("sale_time >", value, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("sale_time >=", value, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeLessThan(Date value) {
            addCriterion("sale_time <", value, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeLessThanOrEqualTo(Date value) {
            addCriterion("sale_time <=", value, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeIn(List<Date> values) {
            addCriterion("sale_time in", values, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeNotIn(List<Date> values) {
            addCriterion("sale_time not in", values, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeBetween(Date value1, Date value2) {
            addCriterion("sale_time between", value1, value2, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleTimeNotBetween(Date value1, Date value2) {
            addCriterion("sale_time not between", value1, value2, "saleTime");
            return (Criteria) this;
        }

        public Criteria andSaleStatusIsNull() {
            addCriterion("sale_status is null");
            return (Criteria) this;
        }

        public Criteria andSaleStatusIsNotNull() {
            addCriterion("sale_status is not null");
            return (Criteria) this;
        }

        public Criteria andSaleStatusEqualTo(Integer value) {
            addCriterion("sale_status =", value, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusNotEqualTo(Integer value) {
            addCriterion("sale_status <>", value, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusGreaterThan(Integer value) {
            addCriterion("sale_status >", value, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("sale_status >=", value, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusLessThan(Integer value) {
            addCriterion("sale_status <", value, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusLessThanOrEqualTo(Integer value) {
            addCriterion("sale_status <=", value, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusIn(List<Integer> values) {
            addCriterion("sale_status in", values, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusNotIn(List<Integer> values) {
            addCriterion("sale_status not in", values, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusBetween(Integer value1, Integer value2) {
            addCriterion("sale_status between", value1, value2, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andSaleStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("sale_status not between", value1, value2, "saleStatus");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeIsNull() {
            addCriterion("departure_time is null");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeIsNotNull() {
            addCriterion("departure_time is not null");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeEqualTo(Date value) {
            addCriterion("departure_time =", value, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeNotEqualTo(Date value) {
            addCriterion("departure_time <>", value, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeGreaterThan(Date value) {
            addCriterion("departure_time >", value, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("departure_time >=", value, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeLessThan(Date value) {
            addCriterion("departure_time <", value, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeLessThanOrEqualTo(Date value) {
            addCriterion("departure_time <=", value, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeIn(List<Date> values) {
            addCriterion("departure_time in", values, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeNotIn(List<Date> values) {
            addCriterion("departure_time not in", values, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeBetween(Date value1, Date value2) {
            addCriterion("departure_time between", value1, value2, "departureTime");
            return (Criteria) this;
        }

        public Criteria andDepartureTimeNotBetween(Date value1, Date value2) {
            addCriterion("departure_time not between", value1, value2, "departureTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeIsNull() {
            addCriterion("arrival_time is null");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeIsNotNull() {
            addCriterion("arrival_time is not null");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeEqualTo(Date value) {
            addCriterion("arrival_time =", value, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeNotEqualTo(Date value) {
            addCriterion("arrival_time <>", value, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeGreaterThan(Date value) {
            addCriterion("arrival_time >", value, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("arrival_time >=", value, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeLessThan(Date value) {
            addCriterion("arrival_time <", value, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeLessThanOrEqualTo(Date value) {
            addCriterion("arrival_time <=", value, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeIn(List<Date> values) {
            addCriterion("arrival_time in", values, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeNotIn(List<Date> values) {
            addCriterion("arrival_time not in", values, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeBetween(Date value1, Date value2) {
            addCriterion("arrival_time between", value1, value2, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andArrivalTimeNotBetween(Date value1, Date value2) {
            addCriterion("arrival_time not between", value1, value2, "arrivalTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andDelFlagIsNull() {
            addCriterion("del_flag is null");
            return (Criteria) this;
        }

        public Criteria andDelFlagIsNotNull() {
            addCriterion("del_flag is not null");
            return (Criteria) this;
        }

        public Criteria andDelFlagEqualTo(Boolean value) {
            addCriterion("del_flag =", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagNotEqualTo(Boolean value) {
            addCriterion("del_flag <>", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagGreaterThan(Boolean value) {
            addCriterion("del_flag >", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagGreaterThanOrEqualTo(Boolean value) {
            addCriterion("del_flag >=", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagLessThan(Boolean value) {
            addCriterion("del_flag <", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagLessThanOrEqualTo(Boolean value) {
            addCriterion("del_flag <=", value, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagIn(List<Boolean> values) {
            addCriterion("del_flag in", values, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagNotIn(List<Boolean> values) {
            addCriterion("del_flag not in", values, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagBetween(Boolean value1, Boolean value2) {
            addCriterion("del_flag between", value1, value2, "delFlag");
            return (Criteria) this;
        }

        public Criteria andDelFlagNotBetween(Boolean value1, Boolean value2) {
            addCriterion("del_flag not between", value1, value2, "delFlag");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}