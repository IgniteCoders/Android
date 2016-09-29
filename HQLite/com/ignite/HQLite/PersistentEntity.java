package com.ignite.HQLite;

import com.ignite.HQLite.annotations.HasMany;
import com.ignite.HQLite.annotations.HasOne;
import com.ignite.HQLite.managers.EntityFieldHelper;
import com.ignite.HQLite.managers.EntityManager;
import com.ignite.HQLite.utils.Reflections;

import java.lang.reflect.Field;
import java.util.List;


public abstract class PersistentEntity {
	
	private Long _id = null;

	public EntityManager getTableData() {
        EntityManager tableData;
        try {
            Field field = getClass().getDeclaredField("TABLE");
            tableData = (EntityManager) field.get(this);
        } catch (Exception e) {
            tableData = null;
        }
        return tableData;
    }

	public PersistentEntity () {
		
	}

    private boolean beforeInsert() {
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
                this._id = insertId;
                afterInsert();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
	}

    private void afterInsert() {
        List<Field> fields = EntityFieldHelper.getRelationFields(getTableData().getDomainClass());
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(HasMany.class)) {
                    HasMany hasMany = field.getAnnotation(HasMany.class);
                    List<PersistentEntity> childs = (List) field.get(this);
                    if (childs != null) {
                        for (PersistentEntity child : childs) {
                            Field childField = Reflections.getDeclaredFieldRecursively(hasMany.mappedBy(), child.getClass(), PersistentEntity.class);
                            boolean accessible = childField.isAccessible();
                            childField.setAccessible(true);
                            childField.set(child, this);
                            childField.setAccessible(accessible);
                            child.insert();
                        }
                    }
                } else if (field.isAnnotationPresent(HasOne.class)) {
                    HasOne hasOne = field.getAnnotation(HasOne.class);
                    PersistentEntity child = (PersistentEntity) field.get(this);
                    if (child != null) {
                        Field childField = Reflections.getDeclaredFieldRecursively(hasOne.mappedBy(), child.getClass(), PersistentEntity.class);
                        boolean accessible = childField.isAccessible();
                        childField.setAccessible(true);
                        childField.set(child, this);
                        childField.setAccessible(accessible);
                        child.insert();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean beforeUpdate() {
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

    private void afterUpdate() {
        List<Field> fields = EntityFieldHelper.getRelationFields(getTableData().getDomainClass());
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(HasMany.class)) {
                    HasMany hasMany = field.getAnnotation(HasMany.class);
                    List<PersistentEntity> childs = (List) field.get(this);
                    if (childs != null) {
                        for (PersistentEntity child : childs) {
                            Field childField = Reflections.getDeclaredFieldRecursively(hasMany.mappedBy(), child.getClass(), PersistentEntity.class);
                            boolean accessible = childField.isAccessible();
                            childField.setAccessible(true);
                            childField.set(child, this);
                            childField.setAccessible(accessible);
                            child.update();
                        }
                    }
                } else if (field.isAnnotationPresent(HasOne.class)) {
                    HasOne hasOne = field.getAnnotation(HasOne.class);
                    PersistentEntity child = (PersistentEntity) field.get(this);
                    if (child != null) {
                        Field childField = Reflections.getDeclaredFieldRecursively(hasOne.mappedBy(), child.getClass(), PersistentEntity.class);
                        boolean accessible = childField.isAccessible();
                        childField.setAccessible(true);
                        childField.set(child, this);
                        childField.setAccessible(accessible);
                        child.update();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
	
	public boolean save() {
		if (this.getTableData().get(_id) == null) {
			return insert();
		} else {
			return update();
		}
	}

    private boolean beforeDelete() {
        List<Field> fields = EntityFieldHelper.getRelationFields(getTableData().getDomainClass());
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(HasMany.class)) {
                    HasMany hasMany = field.getAnnotation(HasMany.class);
                    List<PersistentEntity> childs = (List) field.get(this);
                    if (childs != null) {
                        for (PersistentEntity child : childs) {
                            Field childField = Reflections.getDeclaredFieldRecursively(hasMany.mappedBy(), child.getClass(), PersistentEntity.class);
                            boolean accessible = childField.isAccessible();
                            childField.setAccessible(true);
                            childField.set(child, this);
                            childField.setAccessible(accessible);
                            if(child.delete() == false) {
                                return false;
                            }
                        }
                    }
                } else if (field.isAnnotationPresent(HasOne.class)) {
                    HasOne hasOne = field.getAnnotation(HasOne.class);
                    PersistentEntity child = (PersistentEntity) field.get(this);
                    if (child != null) {
                        Field childField = Reflections.getDeclaredFieldRecursively(hasOne.mappedBy(), child.getClass(), PersistentEntity.class);
                        boolean accessible = childField.isAccessible();
                        childField.setAccessible(true);
                        childField.set(child, this);
                        childField.setAccessible(accessible);
                        if(child.delete() == false) {
                            return false;
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

    private void afterDelete() {
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
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public Long getServerId() {
        return getId();
    }

    public void setServerId(Long id) {
        setId(id);
    }
}
