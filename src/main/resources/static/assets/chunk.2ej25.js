var X=(e,o,t)=>new Promise((i,r)=>{var n=s=>{try{l(t.next(s))}catch(c){r(c)}},a=s=>{try{l(t.throw(s))}catch(c){r(c)}},l=s=>s.done?i(s.value):Promise.resolve(s.value).then(n,a);l((t=t.apply(e,o)).next())});import{p as je,V as We,a as Ve,b as Ue,r as qe,N as Ge,f as _e}from"./chunk.2ej23.js";import{O as Xe,cn as Ze,co as Je,bf as Se,aT as Z,cp as Qe,aS as J,a4 as ee,g as P,ao as $,aB as d,aC as Ye,aD as Ce,bA as eo,aU as he,i as D,bU as Y,aw as R,a$ as T,ax as K,b1 as oe,az as ke,c as x,b6 as Ie,bZ as oo,aZ as Pe,bi as ve,cq as Ne,b2 as V,ci as Q,b9 as no,F as me,cr as to,cs as ro,ct as io,a_ as so,bC as we,ay as A,b4 as ao,b3 as B,bb as le,b5 as E,cu as lo,o as L,a as H,e as pe,t as Re,$ as co,cv as uo,_ as Te,x as $e,w as ze,v as de,A as Be,q as Oe,u as po,W as fo,br as ho,z as vo,G as ce,cw as mo,cx as bo,cy as wo,cz as go,cA as xo,cB as yo,cC as _o,cD as So,cE as Co,cF as ko}from"./bundle.0.0.2.js?v=0.6624411655913371";import{h as ge,c as Io}from"./chunk.2ej34.js";import{c as Po}from"./chunk.2ej35.js";function No(e={},o){const t=Xe({ctrl:!1,command:!1,win:!1,shift:!1,tab:!1}),{keydown:i,keyup:r}=e,n=s=>{switch(s.key){case"Control":t.ctrl=!0;break;case"Meta":t.command=!0,t.win=!0;break;case"Shift":t.shift=!0;break;case"Tab":t.tab=!0;break}i!==void 0&&Object.keys(i).forEach(c=>{if(c!==s.key)return;const f=i[c];if(typeof f=="function")f(s);else{const{stop:_=!1,prevent:S=!1}=f;_&&s.stopPropagation(),S&&s.preventDefault(),f.handler(s)}})},a=s=>{switch(s.key){case"Control":t.ctrl=!1;break;case"Meta":t.command=!1,t.win=!1;break;case"Shift":t.shift=!1;break;case"Tab":t.tab=!1;break}r!==void 0&&Object.keys(r).forEach(c=>{if(c!==s.key)return;const f=r[c];if(typeof f=="function")f(s);else{const{stop:_=!1,prevent:S=!1}=f;_&&s.stopPropagation(),S&&s.preventDefault(),f.handler(s)}})},l=()=>{(o===void 0||o.value)&&(J("keydown",document,n),J("keyup",document,a)),o!==void 0&&ee(o,s=>{s?(J("keydown",document,n),J("keyup",document,a)):(Z("keydown",document,n),Z("keyup",document,a))})};return Ze()?(Je(l),Se(()=>{(o===void 0||o.value)&&(Z("keydown",document,n),Z("keyup",document,a))})):l(),Qe(t)}function Ro(e,o,t){if(!o)return e;const i=P(e.value);let r=null;return ee(e,n=>{r!==null&&window.clearTimeout(r),n===!0?t&&!t.value?i.value=!0:r=window.setTimeout(()=>{i.value=!0},o):i.value=!1}),i}const To=$({name:"ChevronRight",render(){return d("svg",{viewBox:"0 0 16 16",fill:"none",xmlns:"http://www.w3.org/2000/svg"},d("path",{d:"M5.64645 3.14645C5.45118 3.34171 5.45118 3.65829 5.64645 3.85355L9.79289 8L5.64645 12.1464C5.45118 12.3417 5.45118 12.6583 5.64645 12.8536C5.84171 13.0488 6.15829 13.0488 6.35355 12.8536L10.8536 8.35355C11.0488 8.15829 11.0488 7.84171 10.8536 7.64645L6.35355 3.14645C6.15829 2.95118 5.84171 2.95118 5.64645 3.14645Z",fill:"currentColor"}))}}),$o={padding:"4px 0",optionIconSizeSmall:"14px",optionIconSizeMedium:"16px",optionIconSizeLarge:"16px",optionIconSizeHuge:"18px",optionSuffixWidthSmall:"14px",optionSuffixWidthMedium:"14px",optionSuffixWidthLarge:"16px",optionSuffixWidthHuge:"16px",optionIconSuffixWidthSmall:"32px",optionIconSuffixWidthMedium:"32px",optionIconSuffixWidthLarge:"36px",optionIconSuffixWidthHuge:"36px",optionPrefixWidthSmall:"14px",optionPrefixWidthMedium:"14px",optionPrefixWidthLarge:"16px",optionPrefixWidthHuge:"16px",optionIconPrefixWidthSmall:"36px",optionIconPrefixWidthMedium:"36px",optionIconPrefixWidthLarge:"40px",optionIconPrefixWidthHuge:"40px"};function zo(e){const{primaryColor:o,textColor2:t,dividerColor:i,hoverColor:r,popoverColor:n,invertedColor:a,borderRadius:l,fontSizeSmall:s,fontSizeMedium:c,fontSizeLarge:f,fontSizeHuge:_,heightSmall:S,heightMedium:m,heightLarge:C,heightHuge:k,textColor3:y,opacityDisabled:N}=e;return Object.assign(Object.assign({},$o),{optionHeightSmall:S,optionHeightMedium:m,optionHeightLarge:C,optionHeightHuge:k,borderRadius:l,fontSizeSmall:s,fontSizeMedium:c,fontSizeLarge:f,fontSizeHuge:_,optionTextColor:t,optionTextColorHover:t,optionTextColorActive:o,optionTextColorChildActive:o,color:n,dividerColor:i,suffixColor:t,prefixColor:t,optionColorHover:r,optionColorActive:eo(o,{alpha:.1}),groupHeaderTextColor:y,optionTextColorInverted:"#BBB",optionTextColorHoverInverted:"#FFF",optionTextColorActiveInverted:"#FFF",optionTextColorChildActiveInverted:"#FFF",colorInverted:a,dividerColorInverted:"#BBB",suffixColorInverted:"#BBB",prefixColorInverted:"#BBB",optionColorHoverInverted:o,optionColorActiveInverted:o,groupHeaderTextColorInverted:"#AAA",optionOpacityDisabled:N})}const Bo=Ye({name:"Dropdown",common:Ce,peers:{Popover:je},self:zo}),Oo=Bo,be=he("n-dropdown-menu"),ne=he("n-dropdown"),xe=he("n-dropdown-option"),Ae=$({name:"DropdownDivider",props:{clsPrefix:{type:String,required:!0}},render(){return d("div",{class:`${this.clsPrefix}-dropdown-divider`})}}),Ao=$({name:"DropdownGroupHeader",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0}},setup(){const{showIconRef:e,hasSubmenuRef:o}=D(be),{renderLabelRef:t,labelFieldRef:i,nodePropsRef:r,renderOptionRef:n}=D(ne);return{labelField:i,showIcon:e,hasSubmenu:o,renderLabel:t,nodeProps:r,renderOption:n}},render(){var e;const{clsPrefix:o,hasSubmenu:t,showIcon:i,nodeProps:r,renderLabel:n,renderOption:a}=this,{rawNode:l}=this.tmNode,s=d("div",Object.assign({class:`${o}-dropdown-option`},r==null?void 0:r(l)),d("div",{class:`${o}-dropdown-option-body ${o}-dropdown-option-body--group`},d("div",{"data-dropdown-option":!0,class:[`${o}-dropdown-option-body__prefix`,i&&`${o}-dropdown-option-body__prefix--show-icon`]},Y(l.icon)),d("div",{class:`${o}-dropdown-option-body__label`,"data-dropdown-option":!0},n?n(l):Y((e=l.title)!==null&&e!==void 0?e:l[this.labelField])),d("div",{class:[`${o}-dropdown-option-body__suffix`,t&&`${o}-dropdown-option-body__suffix--has-submenu`],"data-dropdown-option":!0})));return a?a({node:s,option:l}):s}});function Ko(e){const{textColorBase:o,opacity1:t,opacity2:i,opacity3:r,opacity4:n,opacity5:a}=e;return{color:o,opacity1Depth:t,opacity2Depth:i,opacity3Depth:r,opacity4Depth:n,opacity5Depth:a}}const Do={name:"Icon",common:Ce,self:Ko},Lo=Do,Ho=R("icon",`
 height: 1em;
 width: 1em;
 line-height: 1em;
 text-align: center;
 display: inline-block;
 position: relative;
 fill: currentColor;
 transform: translateZ(0);
`,[T("color-transition",{transition:"color .3s var(--n-bezier)"}),T("depth",{color:"var(--n-color)"},[K("svg",{opacity:"var(--n-opacity)",transition:"opacity .3s var(--n-bezier)"})]),K("svg",{height:"1em",width:"1em"})]),Fo=Object.assign(Object.assign({},oe.props),{depth:[String,Number],size:[Number,String],color:String,component:[Object,Function]}),Mo=$({_n_icon__:!0,name:"Icon",inheritAttrs:!1,props:Fo,setup(e){const{mergedClsPrefixRef:o,inlineThemeDisabled:t}=ke(e),i=oe("Icon","-icon",Ho,Lo,e,o),r=x(()=>{const{depth:a}=e,{common:{cubicBezierEaseInOut:l},self:s}=i.value;if(a!==void 0){const{color:c,[`opacity${a}Depth`]:f}=s;return{"--n-bezier":l,"--n-color":c,"--n-opacity":f}}return{"--n-bezier":l,"--n-color":"","--n-opacity":""}}),n=t?Ie("icon",x(()=>`${e.depth||"d"}`),r,e):void 0;return{mergedClsPrefix:o,mergedStyle:x(()=>{const{size:a,color:l}=e;return{fontSize:oo(a),color:l}}),cssVars:t?void 0:r,themeClass:n==null?void 0:n.themeClass,onRender:n==null?void 0:n.onRender}},render(){var e;const{$parent:o,depth:t,mergedClsPrefix:i,component:r,onRender:n,themeClass:a}=this;return!((e=o==null?void 0:o.$options)===null||e===void 0)&&e._n_icon__&&Pe("icon","don't wrap `n-icon` inside `n-icon`"),n==null||n(),d("i",ve(this.$attrs,{role:"img",class:[`${i}-icon`,a,{[`${i}-icon--depth`]:t,[`${i}-icon--color-transition`]:t!==void 0}],style:[this.cssVars,this.mergedStyle]}),r?d(r):this.$slots)}});function fe(e,o){return e.type==="submenu"||e.type===void 0&&e[o]!==void 0}function Eo(e){return e.type==="group"}function Ke(e){return e.type==="divider"}function jo(e){return e.type==="render"}const De=$({name:"DropdownOption",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0},parentKey:{type:[String,Number],default:null},placement:{type:String,default:"right-start"},props:Object,scrollable:Boolean},setup(e){const o=D(ne),{hoverKeyRef:t,keyboardKeyRef:i,lastToggledSubmenuKeyRef:r,pendingKeyPathRef:n,activeKeyPathRef:a,animatedRef:l,mergedShowRef:s,renderLabelRef:c,renderIconRef:f,labelFieldRef:_,childrenFieldRef:S,renderOptionRef:m,nodePropsRef:C,menuPropsRef:k}=o,y=D(xe,null),N=D(be),U=D(Ne),te=x(()=>e.tmNode.rawNode),q=x(()=>{const{value:p}=S;return fe(e.tmNode.rawNode,p)}),re=x(()=>{const{disabled:p}=e.tmNode;return p}),ie=x(()=>{if(!q.value)return!1;const{key:p,disabled:g}=e.tmNode;if(g)return!1;const{value:z}=t,{value:F}=i,{value:ae}=r,{value:M}=n;return z!==null?M.includes(p):F!==null?M.includes(p)&&M[M.length-1]!==p:ae!==null?M.includes(p):!1}),se=x(()=>i.value===null&&!l.value),G=Ro(ie,300,se),j=x(()=>!!(y!=null&&y.enteringSubmenuRef.value)),W=P(!1);V(xe,{enteringSubmenuRef:W});function O(){W.value=!0}function u(){W.value=!1}function w(){const{parentKey:p,tmNode:g}=e;g.disabled||s.value&&(r.value=p,i.value=null,t.value=g.key)}function v(){const{tmNode:p}=e;p.disabled||s.value&&t.value!==p.key&&w()}function h(p){if(e.tmNode.disabled||!s.value)return;const{relatedTarget:g}=p;g&&!ge({target:g},"dropdownOption")&&!ge({target:g},"scrollbarRail")&&(t.value=null)}function I(){const{value:p}=q,{tmNode:g}=e;s.value&&!p&&!g.disabled&&(o.doSelect(g.key,g.rawNode),o.doUpdateShow(!1))}return{labelField:_,renderLabel:c,renderIcon:f,siblingHasIcon:N.showIconRef,siblingHasSubmenu:N.hasSubmenuRef,menuProps:k,popoverBody:U,animated:l,mergedShowSubmenu:x(()=>G.value&&!j.value),rawNode:te,hasSubmenu:q,pending:Q(()=>{const{value:p}=n,{key:g}=e.tmNode;return p.includes(g)}),childActive:Q(()=>{const{value:p}=a,{key:g}=e.tmNode,z=p.findIndex(F=>g===F);return z===-1?!1:z<p.length-1}),active:Q(()=>{const{value:p}=a,{key:g}=e.tmNode,z=p.findIndex(F=>g===F);return z===-1?!1:z===p.length-1}),mergedDisabled:re,renderOption:m,nodeProps:C,handleClick:I,handleMouseMove:v,handleMouseEnter:w,handleMouseLeave:h,handleSubmenuBeforeEnter:O,handleSubmenuAfterEnter:u}},render(){var e,o;const{animated:t,rawNode:i,mergedShowSubmenu:r,clsPrefix:n,siblingHasIcon:a,siblingHasSubmenu:l,renderLabel:s,renderIcon:c,renderOption:f,nodeProps:_,props:S,scrollable:m}=this;let C=null;if(r){const U=(e=this.menuProps)===null||e===void 0?void 0:e.call(this,i,i.children);C=d(Le,Object.assign({},U,{clsPrefix:n,scrollable:this.scrollable,tmNodes:this.tmNode.children,parentKey:this.tmNode.key}))}const k={class:[`${n}-dropdown-option-body`,this.pending&&`${n}-dropdown-option-body--pending`,this.active&&`${n}-dropdown-option-body--active`,this.childActive&&`${n}-dropdown-option-body--child-active`,this.mergedDisabled&&`${n}-dropdown-option-body--disabled`],onMousemove:this.handleMouseMove,onMouseenter:this.handleMouseEnter,onMouseleave:this.handleMouseLeave,onClick:this.handleClick},y=_==null?void 0:_(i),N=d("div",Object.assign({class:[`${n}-dropdown-option`,y==null?void 0:y.class],"data-dropdown-option":!0},y),d("div",ve(k,S),[d("div",{class:[`${n}-dropdown-option-body__prefix`,a&&`${n}-dropdown-option-body__prefix--show-icon`]},[c?c(i):Y(i.icon)]),d("div",{"data-dropdown-option":!0,class:`${n}-dropdown-option-body__label`},s?s(i):Y((o=i[this.labelField])!==null&&o!==void 0?o:i.title)),d("div",{"data-dropdown-option":!0,class:[`${n}-dropdown-option-body__suffix`,l&&`${n}-dropdown-option-body__suffix--has-submenu`]},this.hasSubmenu?d(Mo,null,{default:()=>d(To,null)}):null)]),this.hasSubmenu?d(We,null,{default:()=>[d(Ve,null,{default:()=>d("div",{class:`${n}-dropdown-offset-container`},d(Ue,{show:this.mergedShowSubmenu,placement:this.placement,to:m&&this.popoverBody||void 0,teleportDisabled:!m},{default:()=>d("div",{class:`${n}-dropdown-menu-wrapper`},t?d(no,{onBeforeEnter:this.handleSubmenuBeforeEnter,onAfterEnter:this.handleSubmenuAfterEnter,name:"fade-in-scale-up-transition",appear:!0},{default:()=>C}):C)}))})]}):null);return f?f({node:N,option:i}):N}}),Wo=$({name:"NDropdownGroup",props:{clsPrefix:{type:String,required:!0},tmNode:{type:Object,required:!0},parentKey:{type:[String,Number],default:null}},render(){const{tmNode:e,parentKey:o,clsPrefix:t}=this,{children:i}=e;return d(me,null,d(Ao,{clsPrefix:t,tmNode:e,key:e.key}),i==null?void 0:i.map(r=>{const{rawNode:n}=r;return n.show===!1?null:Ke(n)?d(Ae,{clsPrefix:t,key:r.key}):r.isGroup?(Pe("dropdown","`group` node is not allowed to be put in `group` node."),null):d(De,{clsPrefix:t,tmNode:r,parentKey:o,key:r.key})}))}}),Vo=$({name:"DropdownRenderOption",props:{tmNode:{type:Object,required:!0}},render(){const{rawNode:{render:e,props:o}}=this.tmNode;return d("div",o,[e==null?void 0:e()])}}),Le=$({name:"DropdownMenu",props:{scrollable:Boolean,showArrow:Boolean,arrowStyle:[String,Object],clsPrefix:{type:String,required:!0},tmNodes:{type:Array,default:()=>[]},parentKey:{type:[String,Number],default:null}},setup(e){const{renderIconRef:o,childrenFieldRef:t}=D(ne);V(be,{showIconRef:x(()=>{const r=o.value;return e.tmNodes.some(n=>{var a;if(n.isGroup)return(a=n.children)===null||a===void 0?void 0:a.some(({rawNode:s})=>r?r(s):s.icon);const{rawNode:l}=n;return r?r(l):l.icon})}),hasSubmenuRef:x(()=>{const{value:r}=t;return e.tmNodes.some(n=>{var a;if(n.isGroup)return(a=n.children)===null||a===void 0?void 0:a.some(({rawNode:s})=>fe(s,r));const{rawNode:l}=n;return fe(l,r)})})});const i=P(null);return V(to,null),V(ro,null),V(Ne,i),{bodyRef:i}},render(){const{parentKey:e,clsPrefix:o,scrollable:t}=this,i=this.tmNodes.map(r=>{const{rawNode:n}=r;return n.show===!1?null:jo(n)?d(Vo,{tmNode:r,key:r.key}):Ke(n)?d(Ae,{clsPrefix:o,key:r.key}):Eo(n)?d(Wo,{clsPrefix:o,tmNode:r,parentKey:e,key:r.key}):d(De,{clsPrefix:o,tmNode:r,parentKey:e,key:r.key,props:n.props,scrollable:t})});return d("div",{class:[`${o}-dropdown-menu`,t&&`${o}-dropdown-menu--scrollable`],ref:"bodyRef"},t?d(io,{contentClass:`${o}-dropdown-menu__content`},{default:()=>i}):i,this.showArrow?qe({clsPrefix:o,arrowStyle:this.arrowStyle,arrowClass:void 0,arrowWrapperClass:void 0,arrowWrapperStyle:void 0}):null)}}),Uo=R("dropdown-menu",`
 transform-origin: var(--v-transform-origin);
 background-color: var(--n-color);
 border-radius: var(--n-border-radius);
 box-shadow: var(--n-box-shadow);
 position: relative;
 transition:
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
`,[so(),R("dropdown-option",`
 position: relative;
 `,[K("a",`
 text-decoration: none;
 color: inherit;
 outline: none;
 `,[K("&::before",`
 content: "";
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 `)]),R("dropdown-option-body",`
 display: flex;
 cursor: pointer;
 position: relative;
 height: var(--n-option-height);
 line-height: var(--n-option-height);
 font-size: var(--n-font-size);
 color: var(--n-option-text-color);
 transition: color .3s var(--n-bezier);
 `,[K("&::before",`
 content: "";
 position: absolute;
 top: 0;
 bottom: 0;
 left: 4px;
 right: 4px;
 transition: background-color .3s var(--n-bezier);
 border-radius: var(--n-border-radius);
 `),we("disabled",[T("pending",`
 color: var(--n-option-text-color-hover);
 `,[A("prefix, suffix",`
 color: var(--n-option-text-color-hover);
 `),K("&::before","background-color: var(--n-option-color-hover);")]),T("active",`
 color: var(--n-option-text-color-active);
 `,[A("prefix, suffix",`
 color: var(--n-option-text-color-active);
 `),K("&::before","background-color: var(--n-option-color-active);")]),T("child-active",`
 color: var(--n-option-text-color-child-active);
 `,[A("prefix, suffix",`
 color: var(--n-option-text-color-child-active);
 `)])]),T("disabled",`
 cursor: not-allowed;
 opacity: var(--n-option-opacity-disabled);
 `),T("group",`
 font-size: calc(var(--n-font-size) - 1px);
 color: var(--n-group-header-text-color);
 `,[A("prefix",`
 width: calc(var(--n-option-prefix-width) / 2);
 `,[T("show-icon",`
 width: calc(var(--n-option-icon-prefix-width) / 2);
 `)])]),A("prefix",`
 width: var(--n-option-prefix-width);
 display: flex;
 justify-content: center;
 align-items: center;
 color: var(--n-prefix-color);
 transition: color .3s var(--n-bezier);
 z-index: 1;
 `,[T("show-icon",`
 width: var(--n-option-icon-prefix-width);
 `),R("icon",`
 font-size: var(--n-option-icon-size);
 `)]),A("label",`
 white-space: nowrap;
 flex: 1;
 z-index: 1;
 `),A("suffix",`
 box-sizing: border-box;
 flex-grow: 0;
 flex-shrink: 0;
 display: flex;
 justify-content: flex-end;
 align-items: center;
 min-width: var(--n-option-suffix-width);
 padding: 0 8px;
 transition: color .3s var(--n-bezier);
 color: var(--n-suffix-color);
 z-index: 1;
 `,[T("has-submenu",`
 width: var(--n-option-icon-suffix-width);
 `),R("icon",`
 font-size: var(--n-option-icon-size);
 `)]),R("dropdown-menu","pointer-events: all;")]),R("dropdown-offset-container",`
 pointer-events: none;
 position: absolute;
 left: 0;
 right: 0;
 top: -4px;
 bottom: -4px;
 `)]),R("dropdown-divider",`
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-divider-color);
 height: 1px;
 margin: 4px 0;
 `),R("dropdown-menu-wrapper",`
 transform-origin: var(--v-transform-origin);
 width: fit-content;
 `),K(">",[R("scrollbar",`
 height: inherit;
 max-height: inherit;
 `)]),we("scrollable",`
 padding: var(--n-padding);
 `),T("scrollable",[A("content",`
 padding: var(--n-padding);
 `)])]),qo={animated:{type:Boolean,default:!0},keyboard:{type:Boolean,default:!0},size:{type:String,default:"medium"},inverted:Boolean,placement:{type:String,default:"bottom"},onSelect:[Function,Array],options:{type:Array,default:()=>[]},menuProps:Function,showArrow:Boolean,renderLabel:Function,renderIcon:Function,renderOption:Function,nodeProps:Function,labelField:{type:String,default:"label"},keyField:{type:String,default:"key"},childrenField:{type:String,default:"children"},value:[String,Number]},Go=Object.keys(_e),Xo=Object.assign(Object.assign(Object.assign({},_e),qo),oe.props),mn=$({name:"Dropdown",inheritAttrs:!1,props:Xo,setup(e){const o=P(!1),t=ao(B(e,"show"),o),i=x(()=>{const{keyField:u,childrenField:w}=e;return Io(e.options,{getKey(v){return v[u]},getDisabled(v){return v.disabled===!0},getIgnored(v){return v.type==="divider"||v.type==="render"},getChildren(v){return v[w]}})}),r=x(()=>i.value.treeNodes),n=P(null),a=P(null),l=P(null),s=x(()=>{var u,w,v;return(v=(w=(u=n.value)!==null&&u!==void 0?u:a.value)!==null&&w!==void 0?w:l.value)!==null&&v!==void 0?v:null}),c=x(()=>i.value.getPath(s.value).keyPath),f=x(()=>i.value.getPath(e.value).keyPath),_=Q(()=>e.keyboard&&t.value);No({keydown:{ArrowUp:{prevent:!0,handler:re},ArrowRight:{prevent:!0,handler:q},ArrowDown:{prevent:!0,handler:ie},ArrowLeft:{prevent:!0,handler:te},Enter:{prevent:!0,handler:se},Escape:U}},_);const{mergedClsPrefixRef:S,inlineThemeDisabled:m}=ke(e),C=oe("Dropdown","-dropdown",Uo,Oo,e,S);V(ne,{labelFieldRef:B(e,"labelField"),childrenFieldRef:B(e,"childrenField"),renderLabelRef:B(e,"renderLabel"),renderIconRef:B(e,"renderIcon"),hoverKeyRef:n,keyboardKeyRef:a,lastToggledSubmenuKeyRef:l,pendingKeyPathRef:c,activeKeyPathRef:f,animatedRef:B(e,"animated"),mergedShowRef:t,nodePropsRef:B(e,"nodeProps"),renderOptionRef:B(e,"renderOption"),menuPropsRef:B(e,"menuProps"),doSelect:k,doUpdateShow:y}),ee(t,u=>{!e.animated&&!u&&N()});function k(u,w){const{onSelect:v}=e;v&&le(v,u,w)}function y(u){const{"onUpdate:show":w,onUpdateShow:v}=e;w&&le(w,u),v&&le(v,u),o.value=u}function N(){n.value=null,a.value=null,l.value=null}function U(){y(!1)}function te(){j("left")}function q(){j("right")}function re(){j("up")}function ie(){j("down")}function se(){const u=G();u!=null&&u.isLeaf&&t.value&&(k(u.key,u.rawNode),y(!1))}function G(){var u;const{value:w}=i,{value:v}=s;return!w||v===null?null:(u=w.getNode(v))!==null&&u!==void 0?u:null}function j(u){const{value:w}=s,{value:{getFirstAvailableNode:v}}=i;let h=null;if(w===null){const I=v();I!==null&&(h=I.key)}else{const I=G();if(I){let p;switch(u){case"down":p=I.getNext();break;case"up":p=I.getPrev();break;case"right":p=I.getChild();break;case"left":p=I.getParent();break}p&&(h=p.key)}}h!==null&&(n.value=null,a.value=h)}const W=x(()=>{const{size:u,inverted:w}=e,{common:{cubicBezierEaseInOut:v},self:h}=C.value,{padding:I,dividerColor:p,borderRadius:g,optionOpacityDisabled:z,[E("optionIconSuffixWidth",u)]:F,[E("optionSuffixWidth",u)]:ae,[E("optionIconPrefixWidth",u)]:M,[E("optionPrefixWidth",u)]:He,[E("fontSize",u)]:Fe,[E("optionHeight",u)]:Me,[E("optionIconSize",u)]:Ee}=h,b={"--n-bezier":v,"--n-font-size":Fe,"--n-padding":I,"--n-border-radius":g,"--n-option-height":Me,"--n-option-prefix-width":He,"--n-option-icon-prefix-width":M,"--n-option-suffix-width":ae,"--n-option-icon-suffix-width":F,"--n-option-icon-size":Ee,"--n-divider-color":p,"--n-option-opacity-disabled":z};return w?(b["--n-color"]=h.colorInverted,b["--n-option-color-hover"]=h.optionColorHoverInverted,b["--n-option-color-active"]=h.optionColorActiveInverted,b["--n-option-text-color"]=h.optionTextColorInverted,b["--n-option-text-color-hover"]=h.optionTextColorHoverInverted,b["--n-option-text-color-active"]=h.optionTextColorActiveInverted,b["--n-option-text-color-child-active"]=h.optionTextColorChildActiveInverted,b["--n-prefix-color"]=h.prefixColorInverted,b["--n-suffix-color"]=h.suffixColorInverted,b["--n-group-header-text-color"]=h.groupHeaderTextColorInverted):(b["--n-color"]=h.color,b["--n-option-color-hover"]=h.optionColorHover,b["--n-option-color-active"]=h.optionColorActive,b["--n-option-text-color"]=h.optionTextColor,b["--n-option-text-color-hover"]=h.optionTextColorHover,b["--n-option-text-color-active"]=h.optionTextColorActive,b["--n-option-text-color-child-active"]=h.optionTextColorChildActive,b["--n-prefix-color"]=h.prefixColor,b["--n-suffix-color"]=h.suffixColor,b["--n-group-header-text-color"]=h.groupHeaderTextColor),b}),O=m?Ie("dropdown",x(()=>`${e.size[0]}${e.inverted?"i":""}`),W,e):void 0;return{mergedClsPrefix:S,mergedTheme:C,tmNodes:r,mergedShow:t,handleAfterLeave:()=>{e.animated&&N()},doUpdateShow:y,cssVars:m?void 0:W,themeClass:O==null?void 0:O.themeClass,onRender:O==null?void 0:O.onRender}},render(){const e=(i,r,n,a,l)=>{var s;const{mergedClsPrefix:c,menuProps:f}=this;(s=this.onRender)===null||s===void 0||s.call(this);const _=(f==null?void 0:f(void 0,this.tmNodes.map(m=>m.rawNode)))||{},S={ref:Po(r),class:[i,`${c}-dropdown`,this.themeClass],clsPrefix:c,tmNodes:this.tmNodes,style:[...n,this.cssVars],showArrow:this.showArrow,arrowStyle:this.arrowStyle,scrollable:this.scrollable,onMouseenter:a,onMouseleave:l};return d(Le,ve(this.$attrs,S,_))},{mergedTheme:o}=this,t={show:this.mergedShow,theme:o.peers.Popover,themeOverrides:o.peerOverrides.Popover,internalOnAfterLeave:this.handleAfterLeave,internalRenderBody:e,onUpdateShow:this.doUpdateShow,"onUpdate:show":void 0};return d(Ge,Object.assign({},lo(this.$props,Go),t),{trigger:()=>{var i,r;return(r=(i=this.$slots).default)===null||r===void 0?void 0:r.call(i)}})}}),Zo={version:"1.1",xmlns:"http://www.w3.org/2000/svg","xmlns:xlink":"http://www.w3.org/1999/xlink",x:"0px",y:"0px",viewBox:"0 0 512 512","enable-background":"new 0 0 512 512","xml:space":"preserve"},Jo=pe("path",{d:`M452.1,49L52.3,265.3c-6,3.3-5.6,12.1,0.6,14.9l68.2,25.7c4,1.5,7.2,4.5,9,8.4l53,109.1c1,4.8,9.9,6.1,10,1.2l-8.1-90.2
	c0.5-6.7,3-13,7.3-18.2l207.3-203.1c1.2-1.2,2.9-1.6,4.5-1.3c3.4,0.8,4.8,4.9,2.6,7.6L228,338c-4,6-6,11-7,18l-10.7,77.9
	c0.9,6.8,6.2,9.4,10.5,3.3l38.5-45.2c2.6-3.7,7.7-4.5,11.3-1.9l99.2,72.3c4.7,3.5,11.4,0.9,12.6-4.9L463.8,58
	C465.3,51.2,458.2,45.7,452.1,49z`},null,-1),Qo=[Jo],bn=$({name:"IosPaperPlane",render:function(o,t){return L(),H("svg",Zo,Qo)}}),Yo={version:"1.1",xmlns:"http://www.w3.org/2000/svg","xmlns:xlink":"http://www.w3.org/1999/xlink",x:"0px",y:"0px",viewBox:"0 0 512 512","enable-background":"new 0 0 512 512","xml:space":"preserve"},en=pe("g",null,[pe("path",{d:`M256,48C141.125,48,48,141.125,48,256s93.125,208,208,208s208-93.125,208-208S370.875,48,256,48z M363,277H149v-42h214V277\r
		z`})],-1),on=[en],wn=$({name:"MdRemoveCircle",render:function(o,t){return L(),H("svg",Yo,on)}});let ue;function nn(){const e=co(),o=P(0),t=D("resize"),i=[{text:"Enter 发送消息",mac:"⏎ 发送消息"},{text:"Shift+Enter 换行",mac:"↑+⏎ 换行"},{text:"Ctrl+Alt+C 进入新话题",mac:"⌃+⌥+C 进入新话题"},{text:"Ctrl+Alt+D 删除当前话题",mac:"⌃+⌥+D 删除当前话题"}],r=P([]),n=()=>{r.value=e?[]:i.map(({text:c,mac:f})=>({outer:!1,text:uo()?f:c}))};n(),t.change(n);const a=c=>(o.value-1==c||c==r.value.length-1)&&o.value!==c,l=(c,f)=>a(f)&&c.outer,s=()=>{o.value=(o.value+1)%r.value.length,r.value.forEach((c,f)=>c.outer=a(f))};return clearInterval(ue),e||(ue=setInterval(()=>{s()},3e3)),Re(()=>{clearInterval(ue)}),{tipIndex:o,tipList:r,isOuter:l}}const tn={class:"tip_text"},rn=["onTransitionend"],sn={__name:"TipText",setup(e){const{tipIndex:o,tipList:t,isOuter:i}=nn();return(r,n)=>(L(),H("div",tn,[(L(!0),H(me,null,$e(de(t),(a,l)=>(L(),H("div",{key:l,class:ze(["tip_item",{enter:de(o)==l,outer:de(i)(a,l)}]),onTransitionend:()=>a.outer=!1},Be(a.text),43,rn))),128))]))}},gn=Te(sn,[["__scopeId","data-v-5e863dcf"]]);function xn(e,o){const t=()=>{if(!e.value)return;const a=e.value.$el.querySelector(".n-input-wrapper"),l=a.querySelector(".n-input__textarea-el");return{wrapper:a,textEa:l}},i=()=>{if(!e.value)return;const{wrapper:a,textEa:l}=t();let s=l.scrollHeight;o.value.length<=0&&(s=0),s=s-18,s=s<62?62:s,a.style.height=s+"px",l.style.height=s+"px"},r=()=>{if(!e.value)return;const{wrapper:a,textEa:l}=t();l.addEventListener("input",i)},n=()=>{const{wrapper:a,textEa:l}=t();l.removeEventListener("input",i)};ee(o,()=>i()),Oe(()=>{r()}),Se(()=>{n()})}const an={class:"list_wrapper"},ln=["onClick","onTransitionend"],dn={__name:"index",props:{list:{type:Array,default:()=>[]},height:{type:String,default:"30px"}},emits:["itemClick"],setup(e,{emit:o}){po(m=>({"058a8f9c":l.value}));const t=o,i=e;let r;const n=P([]),a=P(0),l=x(()=>i.height),s=m=>(a.value-1==m||m==n.value.length-1)&&a.value!==m,c=(m,C)=>s(C)&&m.__outer,f=()=>{a.value=(a.value+1)%n.value.length,n.value.forEach((m,C)=>m.__outer=s(C))},_=()=>{clearInterval(r),r=setInterval(f,3e3)};fo(()=>{n.value=i.list.map(m=>({text:m,__outer:!1})),_()});const S=m=>t("itemClick",m);return Oe(_),Re(()=>{clearInterval(r)}),(m,C)=>(L(),H("div",an,[(L(!0),H(me,null,$e(n.value,(k,y)=>(L(),H("div",{class:ze(["item",{enter:a.value==y,outer:c(k,y)}]),key:y,onClick:N=>S(k),onTransitionend:()=>k.__outer=!1},[ho(m.$slots,"default",{text:k.text},()=>[vo(Be(k.text),1)],!0)],42,ln))),128))]))}},yn=Te(dn,[["__scopeId","data-v-89c346f3"]]);function _n(){const e=P(null);return{scrollRef:e,scrollToBottom:()=>X(this,null,function*(){yield ce(),e.value&&(e.value.scrollTop=e.value.scrollHeight)}),scrollToTop:()=>X(this,null,function*(){yield ce(),e.value&&(e.value.scrollTop=0)}),scrollToBottomIfAtBottom:()=>X(this,null,function*(){yield ce(),e.value&&e.value.scrollHeight-e.value.scrollTop-e.value.clientHeight<=1e3&&(e.value.scrollTop=e.value.scrollHeight)})}}const cn=()=>`\`\`\`js 
{
    isIDE: ${mo()},
    isRider: ${bo()},
    isFleet: ${wo()},
    isVscode: ${go()},
    isPyCharm: ${xo()},
    isHbuilder: ${yo()},
    isWebStorm: ${_o()},
    isPhpStorm: ${So()},
    isAndroidStudio: ${Co()},
    search: ${window.location.search}

    virtual_version: ${ko},

} 
\`\`\``,ye=[{keys:["TALKX_RELOAD"],type:"reload"},{keys:["TALKX_SHOW_VERSION"],type:"show_version"},{keys:["TALKX_ENV_INFO_"],type:"info",extraFun:cn}],Sn=(e,o)=>{for(let t=0;t<ye.length;t++){const{keys:i,type:r,extraFun:n=()=>{}}=ye[t];if(i.includes(e.trim()))return o.find(s=>s.type===r).handler(n()),!0}};export{bn as I,yn as L,wn as M,mn as N,gn as T,Sn as c,xn as i,_n as u};
