package com.king.lib.groupmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * parse menu file
 * codes are referred to MenuInflater
 * <p/>author：Aiden
 * <p/>create time: 2019/5/13 9:05
 */
public class MenuParser {

    /** Menu tag name in XML. */
    private static final String XML_MENU = "menu";

    /** Group tag name in XML. */
    private static final String XML_GROUP = "group";

    /** Item tag name in XML. */
    private static final String XML_ITEM = "item";

    public GroupMenu inflate(Context context, int menuRes) throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getLayout(menuRes);

        AttributeSet attrs = Xml.asAttributeSet(parser);
        int eventType = parser.getEventType();
        GroupMenu menu = new GroupMenu();

        String tagName;
        String unknownTagName = null;
        // This loop will skip to the menu start tag
        do {
            if (eventType == XmlPullParser.START_TAG) {
                tagName = parser.getName();
                if (tagName.equals(XML_MENU)) {
                    // Go to next tag
                    eventType = parser.next();
                    break;
                }

                throw new RuntimeException("Expecting menu, got " + tagName);
            }
            eventType = parser.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);

        boolean reachedEndOfMenu = false;
        boolean lookingForEndOfUnknownTag = false;
        while (!reachedEndOfMenu) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (lookingForEndOfUnknownTag) {
                        break;
                    }

                    tagName = parser.getName();
                    if (tagName.equals(XML_GROUP)) {
//                        menuState.readGroup(attrs);
                    } else if (tagName.equals(XML_ITEM)) {
                        readItem(context, attrs, menu);
                    } else if (tagName.equals(XML_MENU)) {
                        // A menu start tag denotes a submenu for an item
//                        SubMenu subMenu = menuState.addSubMenuItem();
//                        registerMenu(subMenu, attrs);
//
//                        // Parse the submenu into returned SubMenu
//                        parseMenu(parser, attrs, subMenu);
                    } else {
                        lookingForEndOfUnknownTag = true;
                        unknownTagName = tagName;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    tagName = parser.getName();
                    if (lookingForEndOfUnknownTag && tagName.equals(unknownTagName)) {
                        lookingForEndOfUnknownTag = false;
                        unknownTagName = null;
                    } else if (tagName.equals(XML_GROUP)) {
//                        menuState.resetGroup();
                    } else if (tagName.equals(XML_ITEM)) {
                        // Add the item if it hasn't been added (if the item was
                        // a submenu, it would have been added already)
//                        if (!menuState.hasAddedItem()) {
//                            if (menuState.itemActionProvider != null &&
//                                    menuState.itemActionProvider.hasSubMenu()) {
//                                registerMenu(menuState.addSubMenuItem(), attrs);
//                            } else {
//                                registerMenu(menuState.addItem(), attrs);
//                            }
//                        }
                    } else if (tagName.equals(XML_MENU)) {
                        reachedEndOfMenu = true;
                    }
                    break;

                case XmlPullParser.END_DOCUMENT:
                    throw new RuntimeException("Unexpected end of document");
            }

            eventType = parser.next();
        }
        return menu;
    }

    private void readItem(Context context, AttributeSet attrs, GroupMenu menu) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.GMMenuItem);
        int id = a.getResourceId(R.styleable.GMMenuItem_android_id, -1);
        int iconRes = a.getResourceId(R.styleable.GMMenuItem_android_icon, -1);
        String title = a.getText(R.styleable.GMMenuItem_android_title).toString();
        int showAsAction = a.getInt(R.styleable.GMMenuItem_showAsAction, -1);

        GroupMenuItem item = new GroupMenuItem();
        item.setId(id);
        item.setIconRes(iconRes);
        item.setTitle(title);
        item.setShowAsAction(showAsAction);
        if (menu.getItemList() == null) {
            menu.setItemList(new ArrayList<GroupMenuItem>());
        }
        menu.getItemList().add(item);
    }
}
