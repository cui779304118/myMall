package cup.cw.mall.pojo;

import lombok.Data;

import java.util.List;

/**
 * created by cuiwei on 2018/10/24
 * 分页实体类
 */
@Data
public class PageModel {
    private int pageNum;
    private int pageSize;
    private int offset;
    private int count;
    private int totalRecords;
    private int pages;
    private String orderOption;
    private String orderItem;

    private boolean isLastPage;
    private boolean hasNext;

    private List dataList;

    public PageModel(){}

    private void init(){
        int pages = (int)Math.ceil((double) this.totalRecords/this.pageSize) ;
        if (this.pageNum < pages){
            this.hasNext = true;
        }else{
            this.pages = pages;
            this.isLastPage = true;
            this.hasNext = false;
        }
        this.offset = (this.pageNum - 1)* this.pageSize;
        this.count = pageSize;
    }

    public static PageModelBuilder builder(){
        return new PageModelBuilder();
    }

    public static class PageModelBuilder{
        private int pageNum = 1;
        private int pageSize = 10;
        private int totalRecords;
        private String orderOption = "ASC";
        private String orderItem;

        public PageModelBuilder pageNum(int pageNum){
            this.pageNum = pageNum;
            return this;
        }

        public PageModelBuilder pageSize(int pageSize){
            this.pageSize = pageSize;
            return this;
        }

        public PageModelBuilder totalRecords(int totalRecords){
            this.totalRecords = totalRecords;
            return this;
        }

        public PageModelBuilder orderOption(String orderOption){
            this.orderOption = orderOption;
            return this;
        }

        public PageModelBuilder orderItem(String orderItem){
            this.orderItem = orderItem;
            return this;
        }

        public PageModel build(){
            PageModel pageModel = new PageModel();
            pageModel.setPageNum(pageNum);
            pageModel.setPageSize(pageSize);
            pageModel.setTotalRecords(totalRecords);
            pageModel.setOrderItem(orderItem);
            pageModel.setOrderOption(orderOption);
            pageModel.init();
            return pageModel;
        }
    }
}
