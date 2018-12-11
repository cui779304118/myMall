package cup.cw.mall.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class Category {
    private Integer id;

    private Integer parentId;

    private String name;

    private Byte status;

    private Integer sortOrder;

    private Date createTime;

    private Date updateTime;

    public Category(Integer id, Integer parentId, String name, Byte status, Integer sortOrder, Date createTime, Date updateTime) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.status = status;
        this.sortOrder = sortOrder;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Category(){super();}

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    //因为category以id为唯一标志，所以id相同即可
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        Category category = (Category) o;
        return id != null ? id == category.getId() : category.id == null;
    }

    //因为category以id为唯一标志，因此category的hashCode可以用id的hashCode来代替
    public int hashCode(){
        return id != null ? id.hashCode() : 0;
    }

}