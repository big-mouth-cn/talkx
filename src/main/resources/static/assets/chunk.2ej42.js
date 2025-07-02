import{aD as V,bM as d,ax as o,aw as C,a$ as c,bC as N,bc as W,bd as D,ao as F,az as I,b1 as z,bv as K,c as x,b5 as f,b6 as _,aB as q}from"./bundle.0.0.2.js?v=0.6624411655913371";const A={thPaddingSmall:"6px",thPaddingMedium:"12px",thPaddingLarge:"12px",tdPaddingSmall:"6px",tdPaddingMedium:"12px",tdPaddingLarge:"12px"};function G(e){const{dividerColor:r,cardColor:l,modalColor:n,popoverColor:a,tableHeaderColor:s,tableColorStriped:i,textColor1:t,textColor2:b,borderRadius:g,fontWeightStrong:h,lineHeight:p,fontSizeSmall:v,fontSizeMedium:m,fontSizeLarge:u}=e;return Object.assign(Object.assign({},A),{fontSizeSmall:v,fontSizeMedium:m,fontSizeLarge:u,lineHeight:p,borderRadius:g,borderColor:d(l,r),borderColorModal:d(n,r),borderColorPopover:d(a,r),tdColor:l,tdColorModal:n,tdColorPopover:a,tdColorStriped:d(l,i),tdColorStripedModal:d(n,i),tdColorStripedPopover:d(a,i),thColor:d(l,s),thColorModal:d(n,s),thColorPopover:d(a,s),thTextColor:t,tdTextColor:b,thFontWeight:h})}const J={name:"Table",common:V,self:G},Q=J,U=o([C("table",`
 font-size: var(--n-font-size);
 font-variant-numeric: tabular-nums;
 line-height: var(--n-line-height);
 width: 100%;
 border-radius: var(--n-border-radius) var(--n-border-radius) 0 0;
 text-align: left;
 border-collapse: separate;
 border-spacing: 0;
 overflow: hidden;
 background-color: var(--n-td-color);
 border-color: var(--n-merged-border-color);
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 --n-merged-border-color: var(--n-border-color);
 `,[o("th",`
 white-space: nowrap;
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 text-align: inherit;
 padding: var(--n-th-padding);
 vertical-align: inherit;
 text-transform: none;
 border: 0px solid var(--n-merged-border-color);
 font-weight: var(--n-th-font-weight);
 color: var(--n-th-text-color);
 background-color: var(--n-th-color);
 border-bottom: 1px solid var(--n-merged-border-color);
 border-right: 1px solid var(--n-merged-border-color);
 `,[o("&:last-child",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),o("td",`
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 color .3s var(--n-bezier);
 padding: var(--n-td-padding);
 color: var(--n-td-text-color);
 background-color: var(--n-td-color);
 border: 0px solid var(--n-merged-border-color);
 border-right: 1px solid var(--n-merged-border-color);
 border-bottom: 1px solid var(--n-merged-border-color);
 `,[o("&:last-child",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),c("bordered",`
 border: 1px solid var(--n-merged-border-color);
 border-radius: var(--n-border-radius);
 `,[o("tr",[o("&:last-child",[o("td",`
 border-bottom: 0 solid var(--n-merged-border-color);
 `)])])]),c("single-line",[o("th",`
 border-right: 0px solid var(--n-merged-border-color);
 `),o("td",`
 border-right: 0px solid var(--n-merged-border-color);
 `)]),c("single-column",[o("tr",[o("&:not(:last-child)",[o("td",`
 border-bottom: 0px solid var(--n-merged-border-color);
 `)])])]),c("striped",[o("tr:nth-of-type(even)",[o("td","background-color: var(--n-td-color-striped)")])]),N("bottom-bordered",[o("tr",[o("&:last-child",[o("td",`
 border-bottom: 0px solid var(--n-merged-border-color);
 `)])])])]),W(C("table",`
 background-color: var(--n-td-color-modal);
 --n-merged-border-color: var(--n-border-color-modal);
 `,[o("th",`
 background-color: var(--n-th-color-modal);
 `),o("td",`
 background-color: var(--n-td-color-modal);
 `)])),D(C("table",`
 background-color: var(--n-td-color-popover);
 --n-merged-border-color: var(--n-border-color-popover);
 `,[o("th",`
 background-color: var(--n-th-color-popover);
 `),o("td",`
 background-color: var(--n-td-color-popover);
 `)]))]),X=Object.assign(Object.assign({},z.props),{bordered:{type:Boolean,default:!0},bottomBordered:{type:Boolean,default:!0},singleLine:{type:Boolean,default:!0},striped:Boolean,singleColumn:Boolean,size:{type:String,default:"medium"}}),Z=F({name:"Table",props:X,setup(e){const{mergedClsPrefixRef:r,inlineThemeDisabled:l,mergedRtlRef:n}=I(e),a=z("Table","-table",U,Q,e,r),s=K("Table",n,r),i=x(()=>{const{size:b}=e,{self:{borderColor:g,tdColor:h,tdColorModal:p,tdColorPopover:v,thColor:m,thColorModal:u,thColorPopover:P,thTextColor:S,tdTextColor:M,borderRadius:k,thFontWeight:R,lineHeight:T,borderColorModal:B,borderColorPopover:$,tdColorStriped:w,tdColorStripedModal:y,tdColorStripedPopover:L,[f("fontSize",b)]:O,[f("tdPadding",b)]:j,[f("thPadding",b)]:E},common:{cubicBezierEaseInOut:H}}=a.value;return{"--n-bezier":H,"--n-td-color":h,"--n-td-color-modal":p,"--n-td-color-popover":v,"--n-td-text-color":M,"--n-border-color":g,"--n-border-color-modal":B,"--n-border-color-popover":$,"--n-border-radius":k,"--n-font-size":O,"--n-th-color":m,"--n-th-color-modal":u,"--n-th-color-popover":P,"--n-th-font-weight":R,"--n-th-text-color":S,"--n-line-height":T,"--n-td-padding":j,"--n-th-padding":E,"--n-td-color-striped":w,"--n-td-color-striped-modal":y,"--n-td-color-striped-popover":L}}),t=l?_("table",x(()=>e.size[0]),i,e):void 0;return{rtlEnabled:s,mergedClsPrefix:r,cssVars:l?void 0:i,themeClass:t==null?void 0:t.themeClass,onRender:t==null?void 0:t.onRender}},render(){var e;const{mergedClsPrefix:r}=this;return(e=this.onRender)===null||e===void 0||e.call(this),q("table",{class:[`${r}-table`,this.themeClass,{[`${r}-table--rtl`]:this.rtlEnabled,[`${r}-table--bottom-bordered`]:this.bottomBordered,[`${r}-table--bordered`]:this.bordered,[`${r}-table--single-line`]:this.singleLine,[`${r}-table--single-column`]:this.singleColumn,[`${r}-table--striped`]:this.striped}],style:this.cssVars},this.$slots)}});export{Z as N};
