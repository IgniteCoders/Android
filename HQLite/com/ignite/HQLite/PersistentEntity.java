package com.ignite.HQLite;

import com.ignite.HQLite.annotations.HasMany;
import com.ignite.HQLite.annotations.HasOne;
import com.ignite.HQLite.utils.EntityFieldHelper;
import com.ignite.HQLite.utils.Reflections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public abstract class PersistentEntity {
	
	public Long id = null;

	public EntityManager getTableData() {
        EntityManager tableData;
        try {
            Field field = getClass().getDeclaredField(Configuration.ACCESS_PROPERTY);
            tableData = (EntityManager) field.get(this);
        } catch (Exception e) {
            tableData = null;
        }
        return tableData;
    }

	public PersistentEntity () {
		
	}

    public boolean beforeInsert() {
        Class superClass = getTableData().getDomainClass().getSuperclass();
        if (superClass != PersistentEntity.class) {
            try {
                PersistentEntity superObject = ((PersistentEntity) Reflections.getSuperInstanceFromInstance(this));
                if (superObject.insert()) {
                    this.setId(superObject.getId());
                    return true;
                } else {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
	
	public boolean insert() {
        if (beforeInsert() == true) {
            long insertId = getTableData().insert(this);
            if (insertId > -1) {
                this.id = insertId;
                afterInsert();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
	}

    public void afterInsert() {
        List<Field> fields = EntityFieldHelper.getRelationFields(getTableData().getDomainClass());
        for (Field field : fields) {
            try {
                List<PersistentEntity> childs = new ArrayList<PersistentEntity>();
                String mappedBy = "";
                if (field.isAnnotationPresent(HasMany.class)) {
                    HasMany hasMany = field.getAnnotation(HasMany.class);
                    mappedBy = hasMany.mappedBy();
                    childs = (List) field.get(this);
                } else if (field.isAnnotationPresent(HasOne.class)) {
                    HasOne hasOne = field.getAnnotation(HasOne.class);
                    mappedBy = hasOne.mappedBy();
                    childs.add((PersistentEntity) field.get(this));
                }
                if (childs != null) {
                    for (PersistentEntity child : childs) {
                        if (child != null) {
                            Field childField = Reflections.getDeclaredFieldRecursively(mappedBy, child.getClass(), PersistentEntity.class);
                            //                        EntityFieldHelper.setFieldFromValue(this, childField, child);
                            EntityFieldHelper.setFieldFromValue(child, childField, this);
                            child.insert();
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean beforeUpdate() {
        Class superClass = getTableData().getDomainClass().getSuperclass();
        if (superClass != PersistentEntity.class) {
            try {
                PersistentEntity superObject = ((PersistentEntity) Reflections.getSuperInstanceFromInstance(this));
                superObject.setId(this.getId());
                if(superObject.update()) {
                    return true;
                } else {
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
	
	public boolean update () {
        if(beforeUpdate() == true) {
            int updateRows = getTableData().update(this);

            if (updateRows > 0) {
                afterUpdate();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
	}

    public void afterUpdate() {
        List<Field> fields = EntityFieldHelper.getRelationFields(getTableData().getDomainClass());
        for (Field field : fields) {
            try {
                List<PersistentEntity> childs = new ArrayList<PersistentEntity>();
                String mappedBy = "";
                if (field.isAnnotationPresent(HasMany.class)) {
                    HasMany hasMany = field.getAnnotation(HasMany.class);
                    mappedBy = hasMany.mappedBy();
                    childs = (List) field.get(this);
                } else if (field.isAnnotationPresent(HasOne.class)) {
                    HasOne hasOne = field.getAnnotation(HasOne.class);
                    mappedBy = hasOne.mappedBy();
                    childs.add((PersistentEntity) field.get(this));
                }
                if (childs != null) {
                    for (PersistentEntity child : childs) {
                        if (child != null) {
                            Field childField = Reflections.getDeclaredFieldRecursively(mappedBy, child.getClass(), PersistentEntity.class);
    //                        EntityFieldHelper.setFieldFromValue(this, childField, child);
                            EntityFieldHelper.setFieldFromValue(child, childField, this);
                            child.save();
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
	
	public boolean save() {
		if (id != null && this.getTableData().get(id) != null) {
            return update();
		} else {
            return insert();
		}
	}

    public boolean beforeDelete() {
        List<Field> fields = EntityFieldHelper.getRelationFields(getTableData().getDomainClass());
        for (Field field : fields) {
            try {
                List<PersistentEntity> childs = new ArrayList<PersistentEntity>();
                String mappedBy = "";
                if (field.isAnnotationPresent(HasMany.class)) {
                    HasMany hasMany = field.getAnnotation(HasMany.class);
                    mappedBy = hasMany.mappedBy();
                    childs = (List) field.get(this);
                } else if (field.isAnnotationPresent(HasOne.class)) {
                    HasOne hasOne = field.getAnnotation(HasOne.class);
                    mappedBy = hasOne.mappedBy();
                    childs.add((PersistentEntity) field.get(this));
                }
                if (childs != null) {
                    for (PersistentEntity child : childs) {
                        if (child != null) {
                            Field childField = Reflections.getDeclaredFieldRecursively(mappedBy, child.getClass(), PersistentEntity.class);
                            //                        EntityFieldHelper.setFieldFromValue(this, childField, child);
                            EntityFieldHelper.setFieldFromValue(child, childField, this);
                            if (child.delete() == false) {
                                return false;
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
	
	public boolean delete () {
        if(beforeDelete() == true) {
            int deletedRows = getTableData().delete(this);
            if (deletedRows > 0) {
                afterDelete();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
	}

    public void afterDelete() {
        Class superClass = getTableData().getDomainClass().getSuperclass();
        if (superClass != PersistentEntity.class) {
            try {
                PersistentEntity superObject = ((PersistentEntity) Reflections.getSuperInstanceFromInstance(this));
                superObject.setId(this.getId());
                superObject.delete();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getServerId() {
        return getId();
    }

    public void setServerId(Long id) {
        setId(id);
    }

    @Override
    public String toString() {
        return getTableData().getTableName() + ": " + this.getServerId();
    }
}
