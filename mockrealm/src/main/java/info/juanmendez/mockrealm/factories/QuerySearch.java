package info.juanmendez.mockrealm.factories;

import org.mockito.internal.util.reflection.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import info.juanmendez.mockrealm.dependencies.Compare;
import info.juanmendez.mockrealm.dependencies.MockUtils;
import io.realm.Case;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.exceptions.RealmException;

/**
 * Created by Juan Mendez on 3/7/2017.
 * www.juanmendez.info
 * contact@juanmendez.info
 */
public class QuerySearch {

    String condition;
    ArrayList<String> types;
    Object[] arguments;
    Object needle;
    ArrayList<Object> needles;

    Class clazz;
    Case casing = Case.SENSITIVE;

    Object left;
    Object right;

    public RealmList<RealmModel> search( String condition, Object[] arguments, RealmList<RealmModel> haystack ) {

        this.condition = condition;
        this.arguments = arguments;
        this.types = new ArrayList<>(Arrays.asList(((String) arguments[0]).split("\\.")));

        this.needle = arguments[1];
        this.clazz = MockUtils.getClass(needle);

        int argsLen = arguments.length;

        if ((clazz == String.class || clazz == String[].class) && argsLen >= 3) {
            casing = (Case) arguments[2];
        }

        if (condition == Compare.between) {
            this.left = arguments[1];
            this.right = arguments[2];
        } else if (condition == Compare.in) {
            needles = new ArrayList<>(Arrays.asList((Object[]) needle));
        }


        if (casing == Case.INSENSITIVE) {

            if (condition == Compare.in) {

                for (int i = 0; i < needles.size(); i++) {
                    needles.set(i, ((String) needles.get(i)).toLowerCase());
                }
            } else {
                this.needle = ((String) needle).toLowerCase();
            }

        }

        RealmList<RealmModel> queriedList = ListFactory.create();

        for (RealmModel realmModel : haystack) {
            if (checkRealmObject(realmModel, 0)) {
                queriedList.add(realmModel);
            }
        }

        return queriedList;
    }

    private boolean checkRealmObject(RealmModel realmModel, int level) {
        //RunTimeErrorException if search field is not found in realmQueryClass

        Object value;

        try {
            value = Whitebox.getInternalState(realmModel, types.get(level));
        } catch (Exception e) {
            throw (new RealmException(MockUtils.getClass(realmModel).getName() + " doesn't have the attribute " + types.get(level)));
        }

        if (value != null) {

            if (level < types.size() - 1) {

                if (value instanceof RealmList) {
                    RealmList<RealmModel> valueList = (RealmList<RealmModel>) value;

                    for (RealmModel rm : valueList) {
                        //at least one item must meet the requirements!
                        if (checkRealmObject(rm, level + 1)) {
                            return true;
                        }
                    }

                    return false;
                } else if (value instanceof RealmModel) {
                    return checkRealmObject((RealmModel) value, level + 1);
                }

                throw (new RealmException(types.get(level) + " is of neither type RealmList, or RealmModel"));
            }

            if (condition == Compare.equal) {

                if (clazz == Date.class && (((Date) value)).compareTo((Date) needle) == 0) {
                    return true;
                } else if (casing == Case.INSENSITIVE) {
                    return needle.equals(((String) value).toLowerCase());
                } else if (needle.equals(value)) {
                    return true;
                }
            } else if (condition == Compare.not_equal) {

                if (clazz == Date.class && (((Date) value)).compareTo((Date) needle) != 0) {
                    return true;
                } else if (casing == Case.INSENSITIVE) {
                    return !needle.equals(((String) value).toLowerCase());
                } else if (!needle.equals(value)) {
                    return true;
                }
            } else if (condition == Compare.less) {

                if (clazz == Date.class && (((Date) value)).compareTo((Date) needle) < 0) {
                    return true;
                } else if (clazz == Byte.class && ((byte) value) < ((byte) needle)) {
                    return true;
                } else if (clazz == Integer.class && ((int) value) < ((int) needle)) {
                    return true;
                } else if (clazz == Double.class && ((double) value) < ((double) needle)) {
                    return true;
                } else if (clazz == Long.class && ((long) value) < ((long) needle)) {
                    return true;
                } else if (clazz == Float.class && ((float) value) < ((float) needle)) {
                    return true;
                } else if (clazz == Short.class && ((short) value) < ((short) needle)) {
                    return true;
                }
            } else if (condition == Compare.lessOrEqual) {

                if (clazz == Date.class && (((Date) value)).compareTo((Date) needle) <= 0) {
                    return true;
                } else if (clazz == Byte.class && ((byte) value) <= ((byte) needle)) {
                    return true;
                } else if (clazz == Integer.class && ((int) value) <= ((int) needle)) {
                    return true;
                } else if (clazz == Double.class && ((double) value) <= ((double) needle)) {
                    return true;
                } else if (clazz == Long.class && ((long) value) <= ((long) needle)) {
                    return true;
                } else if (clazz == Float.class && ((float) value) <= ((float) needle)) {
                    return true;
                } else if (clazz == Short.class && ((short) value) <= ((short) needle)) {
                    return true;
                }
            } else if (condition == Compare.more) {

                if (clazz == Date.class && (((Date) value)).compareTo((Date) needle) > 0) {
                    return true;
                } else if (clazz == Byte.class && ((byte) value) > ((byte) needle)) {
                    return true;
                } else if (clazz == Integer.class && ((int) value) > ((int) needle)) {
                    return true;
                } else if (clazz == Double.class && ((double) value) > ((double) needle)) {
                    return true;
                } else if (clazz == Long.class && ((long) value) > ((long) needle)) {
                    return true;
                } else if (clazz == Float.class && ((float) value) > ((float) needle)) {
                    return true;
                } else if (clazz == Short.class && ((short) value) > ((short) needle)) {
                    return true;
                }
            } else if (condition == Compare.moreOrEqual) {

                if (clazz == Date.class && (((Date) value)).compareTo((Date) needle) >= 0) {
                    return true;
                } else if (clazz == Byte.class && ((byte) value) >= ((byte) needle)) {
                    return true;
                } else if (clazz == Integer.class && ((int) value) >= ((int) needle)) {
                    return true;
                } else if (clazz == Double.class && ((double) value) >= ((double) needle)) {
                    return true;
                } else if (clazz == Long.class && ((long) value) >= ((long) needle)) {
                    return true;
                } else if (clazz == Float.class && ((float) value) >= ((float) needle)) {
                    return true;
                } else if (clazz == Short.class && ((short) value) >= ((short) needle)) {
                    return true;
                }
            } else if (condition == Compare.between) {

                if (clazz == Date.class) {

                    if (((Date) value).getTime() >= ((Date) left).getTime() && ((Date) value).getTime() <= ((Date) right).getTime())
                        return true;
                } else if (clazz == Integer.class && ((int) value) >= ((int) left) && ((int) value) <= ((int) right)) {
                    return true;
                } else if (clazz == Double.class && ((double) value) >= ((double) left) && ((double) value) <= ((double) right)) {
                    return true;
                } else if (clazz == Long.class && ((long) value) >= ((long) left) && ((long) value) <= ((long) right)) {
                    return true;
                } else if (clazz == Float.class && ((float) value) >= ((float) left) && ((float) value) <= ((float) right)) {
                    return true;
                } else if (clazz == Short.class && ((short) value) >= ((short) left) && ((short) value) <= ((short) right)) {
                    return true;
                }
            } else if (condition == Compare.in) {

                if (clazz == Date[].class) {

                    for (Object needle : needles) {
                        if ((((Date) value)).compareTo((Date) needle) == 0)
                            return true;
                    }
                } else {
                    if (clazz == String[].class && casing == Case.INSENSITIVE) {
                        return needles.contains(((String) value).toLowerCase());
                    } else {
                        return needles.contains(value);
                    }
                }
            } else if (clazz == String.class && (condition == Compare.contains || condition == Compare.endsWith)) {

                if (condition == Compare.contains) {
                    if (casing == Case.SENSITIVE && ((String) value).contains((String) needle)) {
                        return true;
                    } else if (casing == Case.INSENSITIVE && (((String) value).toLowerCase()).contains(((String) needle))) {
                        return true;
                    }
                } else if (condition == Compare.endsWith) {
                    if (casing == Case.SENSITIVE && ((String) value).endsWith((String) needle)) {
                        return true;
                    } else if (casing == Case.INSENSITIVE && (((String) value).toLowerCase()).endsWith(((String) needle))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
